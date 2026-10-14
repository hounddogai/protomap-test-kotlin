package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled proof of delivery entry is in its life cycle. */
enum class ScheduledProofOfDeliveryEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A scheduled proof of delivery entry as the proof of delivery team keeps it. */
data class ScheduledProofOfDeliveryEntry(
    var code: String,
    var name: String,
    var maximum: Double,
    var attempts: Int,
    var title: String,
    var status: ScheduledProofOfDeliveryEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledProofOfDeliveryEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledProofOfDeliveryEntryStatus): Boolean {
        when (status) {
            ScheduledProofOfDeliveryEntryStatus.CREATED -> {
                when (next) {
                    ScheduledProofOfDeliveryEntryStatus.VALIDATED -> return true
                }
                return false
            }
            ScheduledProofOfDeliveryEntryStatus.VALIDATED -> return next == ScheduledProofOfDeliveryEntryStatus.RETIRED
            ScheduledProofOfDeliveryEntryStatus.PUBLISHED -> {
                when (next) {
                    ScheduledProofOfDeliveryEntryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledProofOfDeliveryEntryStatus) {
        if (!canMoveTo(next)) {
            error("scheduled proof of delivery entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled proof of delivery entry"} $code"

    fun netValue(): Double = round(attempts.toDouble() * maximum * 100) / 100

    fun snapshot(): ScheduledProofOfDeliveryEntry {
        return ScheduledProofOfDeliveryEntry(
            code = code,
            name = name,
            maximum = maximum,
            attempts = attempts,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled proof of delivery entry in status created. */
fun newScheduledProofOfDeliveryEntry(code: String, name: String): ScheduledProofOfDeliveryEntry {
    return ScheduledProofOfDeliveryEntry(
        code = code,
        name = name,
        maximum = 0.0,
        attempts = 0,
        title = "",
        status = ScheduledProofOfDeliveryEntryStatus.CREATED,
    )
}
