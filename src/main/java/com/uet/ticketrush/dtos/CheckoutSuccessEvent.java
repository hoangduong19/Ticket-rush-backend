package com.uet.ticketrush.dtos;

import com.uet.ticketrush.models.Seat;
import com.uet.ticketrush.models.SeatHold;

import java.util.Set;

public record CheckoutSuccessEvent (
        SeatHold hold,
        Set<Seat> seats
) {
}
