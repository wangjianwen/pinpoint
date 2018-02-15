package com.navercorp.pinpoint.plugin.grpc;

/**
 * Created by Administrator on 2018/2/14.
 */
public class TraceInfo {
    private String transactionId;
    private String spanId;
    private String parentSpanId;
    private String applicationType;
    private String applicationName;
    private String metaFlags;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getMetaFlags() {
        return metaFlags;
    }

    public void setMetaFlags(String metaFlags) {
        this.metaFlags = metaFlags;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}