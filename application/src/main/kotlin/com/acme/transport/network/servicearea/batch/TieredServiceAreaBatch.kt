package com.acme.transport.network.servicearea.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered service area batch records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a tiered service area batch is in its life cycle. */
enum class TieredServiceAreaBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered service area batch as the service area team keeps it. */
data class TieredServiceAreaBatch(
    var code: String,
    var title: String,
    var description: String,
    var country: String,
    var active: Boolean,
    var timezone: String,
    var maximum: Double,
    var floorValue: Double,
    var shiftCode: String,
    var status: TieredServiceAreaBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredServiceAreaBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredServiceAreaBatchStatus): Boolean {
        when (status) {
            TieredServiceAreaBatchStatus.OPEN -> {
                when (next) {
                    TieredServiceAreaBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredServiceAreaBatchStatus.IN_REVIEW -> return next == TieredServiceAreaBatchStatus.REOPENED
            TieredServiceAreaBatchStatus.RESOLVED -> {
                when (next) {
                    TieredServiceAreaBatchStatus.REOPENED, TieredServiceAreaBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredServiceAreaBatchStatus.REOPENED -> return next == TieredServiceAreaBatchStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredServiceAreaBatchStatus) {
        if (!canMoveTo(next)) {
            error("tiered service area batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedExposure(): Double = round((maximum - floorValue) * 100) / 100

    fun snapshot(): TieredServiceAreaBatch {
        return TieredServiceAreaBatch(
            code = code,
            title = title,
            description = description,
            country = country,
            active = active,
            timezone = timezone,
            maximum = maximum,
            floorValue = floorValue,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tiered service area batch in status open. */
fun newTieredServiceAreaBatch(code: String, title: String): TieredServiceAreaBatch {
    return TieredServiceAreaBatch(
        code = code,
        title = title,
        description = "",
        country = "",
        active = false,
        timezone = "",
        maximum = 0.0,
        floorValue = 0.0,
        shiftCode = "",
        status = TieredServiceAreaBatchStatus.OPEN,
    )
}
