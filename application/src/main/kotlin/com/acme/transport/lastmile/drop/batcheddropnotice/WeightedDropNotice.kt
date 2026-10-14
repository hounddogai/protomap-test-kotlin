package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted drop notice records of lastmile's drop work, and the rules, stores, and calculations around
// them.

/** Where a weighted drop notice is in its life cycle. */
enum class WeightedDropNoticeStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a weighted drop notice and its current status. */
data class WeightedDropNotice(
    var code: String,
    var name: String,
    var startsAt: Int,
    var minimum: Double,
    var contactEmail: String,
    var updatedAt: Int,
    var vehicleId: String,
    var region: String,
    var loadKg: Double,
    var score: Double,
    var status: WeightedDropNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDropNoticeStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDropNoticeStatus): Boolean {
        when (status) {
            WeightedDropNoticeStatus.OPEN -> {
                when (next) {
                    WeightedDropNoticeStatus.RESOLVED, WeightedDropNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedDropNoticeStatus.IN_REVIEW -> return next == WeightedDropNoticeStatus.RESOLVED
            WeightedDropNoticeStatus.RESOLVED -> {
                when (next) {
                    WeightedDropNoticeStatus.REOPENED -> return true
                }
                return false
            }
            WeightedDropNoticeStatus.REOPENED -> {
                when (next) {
                    WeightedDropNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedDropNoticeStatus) {
        if (!canMoveTo(next)) {
            error("weighted drop notice $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"weighted drop notice"} $code"

    fun effectiveLoad(): Double = round((startsAt.toDouble() - minimum) * 100) / 100

    fun snapshot(): WeightedDropNotice {
        return WeightedDropNotice(
            code = code,
            name = name,
            startsAt = startsAt,
            minimum = minimum,
            contactEmail = contactEmail,
            updatedAt = updatedAt,
            vehicleId = vehicleId,
            region = region,
            loadKg = loadKg,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a weighted drop notice in status open. */
fun newWeightedDropNotice(code: String, name: String): WeightedDropNotice {
    return WeightedDropNotice(
        code = code,
        name = name,
        startsAt = 0,
        minimum = 0.0,
        contactEmail = "",
        updatedAt = 0,
        vehicleId = "",
        region = "",
        loadKg = 0.0,
        score = 0.0,
        status = WeightedDropNoticeStatus.OPEN,
    )
}
