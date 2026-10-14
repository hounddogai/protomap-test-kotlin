package com.acme.transport.crewing.swap.swapschedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled swap digest is in its life cycle. */
enum class ScheduledSwapDigestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled swap digest. Its code is unique within a tenant. */
data class ScheduledSwapDigest(
    var code: String,
    var title: String,
    var discount: Double,
    var maximum: Double,
    var ratio: Double,
    var sequence: Int,
    var version: Int,
    var fuelLiters: Double,
    var depot: String,
    var name: String,
    var status: ScheduledSwapDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledSwapDigestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledSwapDigestStatus): Boolean {
        when (status) {
            ScheduledSwapDigestStatus.DRAFT -> return next == ScheduledSwapDigestStatus.CLOSED
            ScheduledSwapDigestStatus.PENDING -> {
                when (next) {
                    ScheduledSwapDigestStatus.ACTIVE, ScheduledSwapDigestStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledSwapDigestStatus.ACTIVE -> {
                when (next) {
                    ScheduledSwapDigestStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledSwapDigestStatus.SUSPENDED -> {
                when (next) {
                    ScheduledSwapDigestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledSwapDigestStatus) {
        if (!canMoveTo(next)) {
            error("scheduled swap digest $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"scheduled swap digest"} $code"

    fun netVolume(): Double = round((discount + ratio) * 100) / 100

    fun snapshot(): ScheduledSwapDigest {
        return ScheduledSwapDigest(
            code = code,
            title = title,
            discount = discount,
            maximum = maximum,
            ratio = ratio,
            sequence = sequence,
            version = version,
            fuelLiters = fuelLiters,
            depot = depot,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scheduled swap digest in status draft. */
fun newScheduledSwapDigest(code: String, title: String): ScheduledSwapDigest {
    return ScheduledSwapDigest(
        code = code,
        title = title,
        discount = 0.0,
        maximum = 0.0,
        ratio = 0.0,
        sequence = 0,
        version = 0,
        fuelLiters = 0.0,
        depot = "",
        name = "",
        status = ScheduledSwapDigestStatus.DRAFT,
    )
}
