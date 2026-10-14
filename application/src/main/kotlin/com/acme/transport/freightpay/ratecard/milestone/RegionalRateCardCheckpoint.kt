package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional rate card checkpoint is in its life cycle. */
enum class RegionalRateCardCheckpointStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A regional rate card checkpoint. Its code is unique within a tenant. */
data class RegionalRateCardCheckpoint(
    var code: String,
    var title: String,
    var sequence: Int,
    var attributes: MutableMap<String, String>,
    var graceDays: Int,
    var status: RegionalRateCardCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRateCardCheckpointStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRateCardCheckpointStatus): Boolean {
        when (status) {
            RegionalRateCardCheckpointStatus.NEW -> {
                when (next) {
                    RegionalRateCardCheckpointStatus.QUEUED -> return true
                }
                return false
            }
            RegionalRateCardCheckpointStatus.QUEUED -> {
                when (next) {
                    RegionalRateCardCheckpointStatus.FAILED -> return true
                }
                return false
            }
            RegionalRateCardCheckpointStatus.RUNNING -> return next == RegionalRateCardCheckpointStatus.FAILED
            RegionalRateCardCheckpointStatus.DONE -> return next == RegionalRateCardCheckpointStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalRateCardCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("regional rate card checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round((graceDays.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): RegionalRateCardCheckpoint {
        return RegionalRateCardCheckpoint(
            code = code,
            title = title,
            sequence = sequence,
            attributes = attributes,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional rate card checkpoint in status new. */
fun newRegionalRateCardCheckpoint(code: String, title: String): RegionalRateCardCheckpoint {
    return RegionalRateCardCheckpoint(
        code = code,
        title = title,
        sequence = 0,
        attributes = mutableMapOf<String, String>(),
        graceDays = 0,
        status = RegionalRateCardCheckpointStatus.NEW,
    )
}
