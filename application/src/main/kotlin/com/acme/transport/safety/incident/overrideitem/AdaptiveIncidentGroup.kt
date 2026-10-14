package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive incident group is in its life cycle. */
enum class AdaptiveIncidentGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One adaptive incident group, with what the planners need to act on it. */
data class AdaptiveIncidentGroup(
    var code: String,
    var name: String,
    var batchSize: Int,
    var phone: String,
    var retries: Int,
    var odometer: Int,
    var sequence: Int,
    var pinned: Boolean,
    var status: AdaptiveIncidentGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveIncidentGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveIncidentGroupStatus): Boolean {
        when (status) {
            AdaptiveIncidentGroupStatus.REQUESTED -> return next == AdaptiveIncidentGroupStatus.GRANTED || next == AdaptiveIncidentGroupStatus.REVOKED
            AdaptiveIncidentGroupStatus.GRANTED -> {
                when (next) {
                    AdaptiveIncidentGroupStatus.REVOKED -> return true
                }
                return false
            }
            AdaptiveIncidentGroupStatus.REVOKED -> return next == AdaptiveIncidentGroupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveIncidentGroupStatus) {
        if (!canMoveTo(next)) {
            error("adaptive incident group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive incident group"} $code"

    fun weightedLoad(): Double = round((retries.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveIncidentGroup {
        return AdaptiveIncidentGroup(
            code = code,
            name = name,
            batchSize = batchSize,
            phone = phone,
            retries = retries,
            odometer = odometer,
            sequence = sequence,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive incident group in status requested. */
fun newAdaptiveIncidentGroup(code: String, name: String): AdaptiveIncidentGroup {
    return AdaptiveIncidentGroup(
        code = code,
        name = name,
        batchSize = 0,
        phone = "",
        retries = 0,
        odometer = 0,
        sequence = 0,
        pinned = false,
        status = AdaptiveIncidentGroupStatus.REQUESTED,
    )
}
