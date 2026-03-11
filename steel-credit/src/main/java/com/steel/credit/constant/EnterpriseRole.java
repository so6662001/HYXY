package com.steel.credit.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnterpriseRole {

    BUYER(1, "买家"),
    SELLER(2, "卖家"),
    BOTH(3, "买卖双角色");

    private final int code;
    private final String description;
}
