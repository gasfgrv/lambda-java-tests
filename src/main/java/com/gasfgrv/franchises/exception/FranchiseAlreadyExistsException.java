package com.gasfgrv.franchises.exception;

public class FranchiseAlreadyExistsException extends RuntimeException {

    public FranchiseAlreadyExistsException(String id) {
        super("Franchise already exists: " + id);
    }

}
