package com.showoff;

public record UserRecord(String name, int age) {
    public UserRecord {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        if (age < 0) {
            throw new IllegalArgumentException("age must be >= 0");
        }
    }
}
