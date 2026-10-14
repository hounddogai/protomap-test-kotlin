package com.acme.transport.emissions.routeoffset.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient route offset exception records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a lenient route offset exception is in its life cycle. */
enum class LenientRouteOffsetExceptionStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a lenient route offset exception and its current status. */
data class LenientRouteOffsetException(
    var code: String,
    var label: String,
    var capacity: Int,
    var source: String,
    var pallets: Int,
    var etaMinutes: Int,
    var endsAt: Int,
    var archived: Boolean,
    var status: LenientRouteOffsetExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRouteOffsetExceptionStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRouteOffsetExceptionStatus): Boolean {
        when (status) {
            LenientRouteOffsetExceptionStatus.CREATED -> return next == LenientRouteOffsetExceptionStatus.PUBLISHED
            LenientRouteOffsetExceptionStatus.VALIDATED -> {
                when (next) {
                    LenientRouteOffsetExceptionStatus.PUBLISHED, LenientRouteOffsetExceptionStatus.RETIRED -> return true
                }
                return false
            }
            LenientRouteOffsetExceptionStatus.PUBLISHED -> {
                when (next) {
                    LenientRouteOffsetExceptionStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientRouteOffsetExceptionStatus) {
        if (!canMoveTo(next)) {
            error("lenient route offset exception $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedValue(): Double = round((endsAt.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): LenientRouteOffsetException {
        return LenientRouteOffsetException(
            code = code,
            label = label,
            capacity = capacity,
            source = source,
            pallets = pallets,
            etaMinutes = etaMinutes,
            endsAt = endsAt,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a lenient route offset exception in status created. */
fun newLenientRouteOffsetException(code: String, label: String): LenientRouteOffsetException {
    return LenientRouteOffsetException(
        code = code,
        label = label,
        capacity = 0,
        source = "",
        pallets = 0,
        etaMinutes = 0,
        endsAt = 0,
        archived = false,
        status = LenientRouteOffsetExceptionStatus.CREATED,
    )
}
