package com.acme.transport.terminal.dwell.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback dwell exception is in its life cycle. */
enum class FallbackDwellExceptionStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fallback dwell exception as the dwell team keeps it. */
data class FallbackDwellException(
    var code: String,
    var name: String,
    var routeCode: String,
    var title: String,
    var ratio: Double,
    var startsAt: Int,
    var status: FallbackDwellExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDwellExceptionStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDwellExceptionStatus): Boolean {
        when (status) {
            FallbackDwellExceptionStatus.CREATED -> return next == FallbackDwellExceptionStatus.RETIRED
            FallbackDwellExceptionStatus.VALIDATED -> {
                when (next) {
                    FallbackDwellExceptionStatus.RETIRED -> return true
                }
                return false
            }
            FallbackDwellExceptionStatus.PUBLISHED -> return next == FallbackDwellExceptionStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackDwellExceptionStatus) {
        if (!canMoveTo(next)) {
            error("fallback dwell exception $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun netTotal(): Double = round((ratio + startsAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackDwellException {
        return FallbackDwellException(
            code = code,
            name = name,
            routeCode = routeCode,
            title = title,
            ratio = ratio,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback dwell exception in status created. */
fun newFallbackDwellException(code: String, name: String): FallbackDwellException {
    return FallbackDwellException(
        code = code,
        name = name,
        routeCode = "",
        title = "",
        ratio = 0.0,
        startsAt = 0,
        status = FallbackDwellExceptionStatus.CREATED,
    )
}
