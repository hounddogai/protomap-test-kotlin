package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped trailer group is in its life cycle. */
enum class CappedTrailerGroupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capped trailer group as the trailer team keeps it. */
data class CappedTrailerGroup(
    var code: String,
    var name: String,
    var segment: String,
    var discount: Double,
    var ratio: Double,
    var capacity: Int,
    var graceDays: Int,
    var channel: String,
    var cost: Double,
    var title: String,
    var unit: String,
    var status: CappedTrailerGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTrailerGroupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTrailerGroupStatus): Boolean {
        when (status) {
            CappedTrailerGroupStatus.NEW -> {
                when (next) {
                    CappedTrailerGroupStatus.QUEUED, CappedTrailerGroupStatus.RUNNING -> return true
                }
                return false
            }
            CappedTrailerGroupStatus.QUEUED -> {
                when (next) {
                    CappedTrailerGroupStatus.RUNNING -> return true
                }
                return false
            }
            CappedTrailerGroupStatus.RUNNING -> {
                when (next) {
                    CappedTrailerGroupStatus.DONE, CappedTrailerGroupStatus.FAILED -> return true
                }
                return false
            }
            CappedTrailerGroupStatus.DONE -> return next == CappedTrailerGroupStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CappedTrailerGroupStatus) {
        if (!canMoveTo(next)) {
            error("capped trailer group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped trailer group"} $code"

    fun netValue(): Double = round((cost - graceDays.toDouble()) * 100) / 100

    fun snapshot(): CappedTrailerGroup {
        return CappedTrailerGroup(
            code = code,
            name = name,
            segment = segment,
            discount = discount,
            ratio = ratio,
            capacity = capacity,
            graceDays = graceDays,
            channel = channel,
            cost = cost,
            title = title,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a capped trailer group in status new. */
fun newCappedTrailerGroup(code: String, name: String): CappedTrailerGroup {
    return CappedTrailerGroup(
        code = code,
        name = name,
        segment = "",
        discount = 0.0,
        ratio = 0.0,
        capacity = 0,
        graceDays = 0,
        channel = "",
        cost = 0.0,
        title = "",
        unit = "",
        status = CappedTrailerGroupStatus.NEW,
    )
}
