package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute profile is in its life cycle. */
enum class DisputeProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A dispute profile. Its code is unique within a tenant. */
data class DisputeProfile(
    var code: String,
    var title: String,
    var floorValue: Double,
    var locale: String,
    var tags: MutableList<String>,
    var ratio: Double,
    var pallets: Int,
    var ownerId: String,
    var status: DisputeProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeProfileStatus): Boolean {
        when (status) {
            DisputeProfileStatus.PLANNED -> {
                when (next) {
                    DisputeProfileStatus.SCHEDULED, DisputeProfileStatus.CANCELLED -> return true
                }
                return false
            }
            DisputeProfileStatus.SCHEDULED -> {
                when (next) {
                    DisputeProfileStatus.IN_PROGRESS -> return true
                }
                return false
            }
            DisputeProfileStatus.IN_PROGRESS -> {
                when (next) {
                    DisputeProfileStatus.COMPLETED -> return true
                }
                return false
            }
            DisputeProfileStatus.COMPLETED -> {
                when (next) {
                    DisputeProfileStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeProfileStatus) {
        if (!canMoveTo(next)) {
            error("dispute profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedExposure(): Double = round((ratio - pallets.toDouble()) * 100) / 100

    fun snapshot(): DisputeProfile {
        return DisputeProfile(
            code = code,
            title = title,
            floorValue = floorValue,
            locale = locale,
            tags = tags,
            ratio = ratio,
            pallets = pallets,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a dispute profile in status planned. */
fun newDisputeProfile(code: String, title: String): DisputeProfile {
    return DisputeProfile(
        code = code,
        title = title,
        floorValue = 0.0,
        locale = "",
        tags = mutableListOf<String>(),
        ratio = 0.0,
        pallets = 0,
        ownerId = "",
        status = DisputeProfileStatus.PLANNED,
    )
}
