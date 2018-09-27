package org.thoughtcrime.securesms;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.thoughtcrime.securesms.util.PushCharacterCalculator;
import org.whispersystems.libsignal.util.guava.Optional;

import java.util.LinkedList;
import java.util.List;

import static org.thoughtcrime.securesms.TransportOption.Type;

public class TransportOptions {

  private static final String TAG = TransportOptions.class.getSimpleName();

  private final List<OnTransportChangedListener> listeners = new LinkedList<>();
  private final Context                          context;
  private final List<TransportOption>            enabledTransports;

  private Type                      defaultTransportType  = Type.TEXTSECURE;
  private Optional<TransportOption> selectedOption        = Optional.absent();

  public TransportOptions(Context context) {
    this.context               = context;
    this.enabledTransports     = initializeAvailableTransports();
  }

  public void reset() {
    List<TransportOption> transportOptions = initializeAvailableTransports();

    this.enabledTransports.clear();
    this.enabledTransports.addAll(transportOptions);

    if (selectedOption.isPresent() && !isEnabled(selectedOption.get())) {
      setSelectedTransport(null);
    } else {
      this.defaultTransportType = Type.TEXTSECURE;

      notifyTransportChangeListeners();
    }
  }

  public void setDefaultTransport(Type type) {
    this.defaultTransportType = type;

    if (!selectedOption.isPresent()) {
      notifyTransportChangeListeners();
    }
  }

  public void setSelectedTransport(@Nullable  TransportOption transportOption) {
    this.selectedOption = Optional.fromNullable(transportOption);
    notifyTransportChangeListeners();
  }

  public boolean isManualSelection() {
    return this.selectedOption.isPresent();
  }

  public @NonNull TransportOption getSelectedTransport() {
    if (selectedOption.isPresent()) return selectedOption.get();

    for (TransportOption transportOption : enabledTransports) {
      if (transportOption.getType() == defaultTransportType) {
        return transportOption;
      }
    }

    throw new AssertionError("No options of default type!");
  }

  public void disableTransport(Type type) {
    Optional<TransportOption> option = find(type);

    if (option.isPresent()) {
      enabledTransports.remove(option.get());

      if (selectedOption.isPresent() && selectedOption.get().getType() == type) {
        setSelectedTransport(null);
      }
    }
  }

  public List<TransportOption> getEnabledTransports() {
    return enabledTransports;
  }

  public void addOnTransportChangedListener(OnTransportChangedListener listener) {
    this.listeners.add(listener);
  }

  private List<TransportOption> initializeAvailableTransports() {
    List<TransportOption> results = new LinkedList<>();

    results.add(new TransportOption(Type.TEXTSECURE, R.drawable.ic_send_sms_white_24dp,
                                    context.getResources().getColor(R.color.textsecure_primary),
                                    context.getString(R.string.ConversationActivity_transport_signal),
                                    context.getString(R.string.conversation_activity__type_message),
                                    new PushCharacterCalculator()));

    return results;
  }

  private void notifyTransportChangeListeners() {
    for (OnTransportChangedListener listener : listeners) {
      listener.onChange(getSelectedTransport(), selectedOption.isPresent());
    }
  }

  private Optional<TransportOption> find(Type type) {
    for (TransportOption option : enabledTransports) {
      if (option.isType(type)) {
        return Optional.of(option);
      }
    }

    return Optional.absent();
  }

  private boolean isEnabled(TransportOption transportOption) {
    for (TransportOption option : enabledTransports) {
      if (option.equals(transportOption)) return true;
    }

    return false;
  }

  public interface OnTransportChangedListener {
    public void onChange(TransportOption newTransport, boolean manuallySelected);
  }
}
