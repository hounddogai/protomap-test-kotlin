package com.acme.transport.customs.broker.metric

import com.acme.transport.kit.text.normalize

/** Where a manual broker reservation is in its life cycle. */
enum class ManualBrokerReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual broker reservation as the broker team keeps it. */
data class ManualBrokerReservation(
    var code: String,
    var title: String,
    var locale: String,
    var ownerId: String,
    var tags: MutableList<String>,
    var phone: String,
    var visible: Boolean,
    var version: Int,
    var status: ManualBrokerReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualBrokerReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualBrokerReservationStatus): Boolean {
        when (status) {
            ManualBrokerReservationStatus.IDLE -> return next == ManualBrokerReservationStatus.DRAINING || next == ManualBrokerReservationStatus.STOPPED
            ManualBrokerReservationStatus.WARMING -> {
                when (next) {
                    ManualBrokerReservationStatus.READY -> return true
                }
                return false
            }
            ManualBrokerReservationStatus.READY -> return next == ManualBrokerReservationStatus.STOPPED
            ManualBrokerReservationStatus.DRAINING -> return next == ManualBrokerReservationStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualBrokerReservationStatus) {
        if (!canMoveTo(next)) {
            error("manual broker reservation $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): ManualBrokerReservation {
        return ManualBrokerReservation(
            code = code,
            title = title,
            locale = locale,
            ownerId = ownerId,
            tags = tags,
            phone = phone,
            visible = visible,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual broker reservation in status idle. */
fun newManualBrokerReservation(code: String, title: String): ManualBrokerReservation {
    return ManualBrokerReservation(
        code = code,
        title = title,
        locale = "",
        ownerId = "",
        tags = mutableListOf<String>(),
        phone = "",
        visible = false,
        version = 0,
        status = ManualBrokerReservationStatus.IDLE,
    )
}
