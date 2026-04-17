package com.barterbay.frontend.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ExchangeRow {

    private final StringProperty requester = new SimpleStringProperty();
    private final StringProperty receiver = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    public ExchangeRow(String requester, String receiver, String status) {
        this.requester.set(requester);
        this.receiver.set(receiver);
        this.status.set(status);
    }

    public String getRequester() {
        return requester.get();
    }

    public StringProperty requesterProperty() {
        return requester;
    }

    public String getReceiver() {
        return receiver.get();
    }

    public StringProperty receiverProperty() {
        return receiver;
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }
}
