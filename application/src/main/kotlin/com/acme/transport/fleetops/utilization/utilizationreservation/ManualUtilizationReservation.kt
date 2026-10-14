package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual utilization reservation records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a manual utilization reservation is in its life cycle. */
enum class ManualUtilizationReservationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A manual utilization reservation. Its code is unique within a tenant. */
data class ManualUtilizationReservation(
    var code: String,
    var name: String,
    var retries: Int,
    var minimum: Double,
    var amount: Double,
    var category: String,
    var title: String,
    var contactEmail: String,
    var tags: MutableList<String>,
    var notes: String,
    var externalRef: String,
    var status: ManualUtilizationReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualUtilizationReservationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualUtilizationReservationStatus): Boolean {
        when (status) {
            ManualUtilizationReservationStatus.OPEN -> {
                when (next) {
                    ManualUtilizationReservationStatus.RESOLVED, ManualUtilizationReservationStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualUtilizationReservationStatus.IN_REVIEW -> {
                when (next) {
                    ManualUtilizationReservationStatus.REOPENED, ManualUtilizationReservationStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualUtilizationReservationStatus.RESOLVED -> {
                when (next) {
                    ManualUtilizationReservationStatus.REOPENED, ManualUtilizationReservationStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualUtilizationReservationStatus.REOPENED -> return next == ManualUtilizationReservationStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ManualUtilizationReservationStatus) {
        if (!canMoveTo(next)) {
            error("manual utilization reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveVolume(): Double = round((retries.toDouble() - amount) * 100) / 100

    fun snapshot(): ManualUtilizationReservation {
        return ManualUtilizationReservation(
            code = code,
            name = name,
            retries = retries,
            minimum = minimum,
            amount = amount,
            category = category,
            title = title,
            contactEmail = contactEmail,
            tags = tags,
            notes = notes,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a manual utilization reservation in status open. */
fun newManualUtilizationReservation(code: String, name: String): ManualUtilizationReservation {
    return ManualUtilizationReservation(
        code = code,
        name = name,
        retries = 0,
        minimum = 0.0,
        amount = 0.0,
        category = "",
        title = "",
        contactEmail = "",
        tags = mutableListOf<String>(),
        notes = "",
        externalRef = "",
        status = ManualUtilizationReservationStatus.OPEN,
    )
}
