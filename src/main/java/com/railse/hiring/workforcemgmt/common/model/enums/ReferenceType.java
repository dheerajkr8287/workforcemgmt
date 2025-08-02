package com.railse.hiring.workforcemgmt.common.model.enums;

public enum ReferenceType {
    ORDER("Order Reference"),
    ENTITY("Entity Reference"),
    CUSTOMER("Customer Reference"),
    PRODUCT("Product Reference");

    private final String description;

    ReferenceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}