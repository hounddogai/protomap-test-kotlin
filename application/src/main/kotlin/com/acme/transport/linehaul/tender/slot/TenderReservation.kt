package com.acme.transport.linehaul.tender.slot

import com.acme.transport.kit.text.normalize

/** Where a tender reservation is in its life cycle. */
enum class TenderReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tender reservation. Its code is unique within a tenant. */
data class TenderReservation(
    var code: String,
    var title: String,
    var timezone: String,
    var contactEmail: String,
    var ownerId: String,
    var dock: String,
    var region: String,
    var lane: String,
    var reviewer: String,
    var amount: Double,
    var status: TenderReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderReservationStatus): Boolean {
        when (status) {
            TenderReservationStatus.IDLE -> {
                when (next) {
                    TenderReservationStatus.WARMING, TenderReservationStatus.DRAINING -> return true
                }
                return false
            }
            TenderReservationStatus.WARMING -> return next == TenderReservationStatus.DRAINING || next == TenderReservationStatus.STOPPED
            TenderReservationStatus.READY -> {
                when (next) {
                    TenderReservationStatus.DRAINING, TenderReservationStatus.STOPPED -> return true
                }
                return false
            }
            TenderReservationStatus.DRAINING -> {
                when (next) {
                    TenderReservationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderReservationStatus) {
        if (!canMoveTo(next)) {
            error("tender reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = amount

    fun snapshot(): TenderReservation {
        return TenderReservation(
            code = code,
            title = title,
            timezone = timezone,
            contactEmail = contactEmail,
            ownerId = ownerId,
            dock = dock,
            region = region,
            lane = lane,
            reviewer = reviewer,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a tender reservation in status idle. */
fun newTenderReservation(code: String, title: String): TenderReservation {
    return TenderReservation(
        code = code,
        title = title,
        timezone = "",
        contactEmail = "",
        ownerId = "",
        dock = "",
        region = "",
        lane = "",
        reviewer = "",
        amount = 0.0,
        status = TenderReservationStatus.IDLE,
    )
}
