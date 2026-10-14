package com.acme.transport.crewing.swap.swapmapping

import com.acme.transport.kit.text.normalize

/** Where a fallback swap segment is in its life cycle. */
enum class FallbackSwapSegmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A fallback swap segment as the swap team keeps it. */
data class FallbackSwapSegment(
    var code: String,
    var name: String,
    var unit: String,
    var retries: Int,
    var category: String,
    var status: FallbackSwapSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackSwapSegmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackSwapSegmentStatus): Boolean {
        when (status) {
            FallbackSwapSegmentStatus.DRAFT -> {
                when (next) {
                    FallbackSwapSegmentStatus.SUSPENDED -> return true
                }
                return false
            }
            FallbackSwapSegmentStatus.PENDING -> return next == FallbackSwapSegmentStatus.ACTIVE
            FallbackSwapSegmentStatus.ACTIVE -> {
                when (next) {
                    FallbackSwapSegmentStatus.SUSPENDED, FallbackSwapSegmentStatus.CLOSED -> return true
                }
                return false
            }
            FallbackSwapSegmentStatus.SUSPENDED -> return next == FallbackSwapSegmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FallbackSwapSegmentStatus) {
        if (!canMoveTo(next)) {
            error("fallback swap segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): FallbackSwapSegment {
        return FallbackSwapSegment(
            code = code,
            name = name,
            unit = unit,
            retries = retries,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fallback swap segment in status draft. */
fun newFallbackSwapSegment(code: String, name: String): FallbackSwapSegment {
    return FallbackSwapSegment(
        code = code,
        name = name,
        unit = "",
        retries = 0,
        category = "",
        status = FallbackSwapSegmentStatus.DRAFT,
    )
}
