package com.codenbugs.adsservice.enums;

import lombok.Getter;

@Getter
public enum AdPeriod {
    ONE_DAY(1),
    THREE_DAYS(3),
    ONE_WEEK(7),
    TWO_WEEKS(14);

    private final int days;

    AdPeriod(int days) { this.days = days; }

}
