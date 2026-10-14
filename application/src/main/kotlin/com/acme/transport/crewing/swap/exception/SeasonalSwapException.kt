package com.acme.transport.crewing.swap.exception

import com.acme.transport.kit.text.normalize

// The seasonal swap exception records of crewing's swap work, and the rules, stores, and calculations around
// them.

/** Where a seasonal swap exception is in its life cycle. */
enum class SeasonalSwapExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A seasonal swap exception as the swap team keeps it. */
data class SeasonalSwapException(
    var code: String,
    var label: String,
    var floorValue: Double,
    var checksum: String,
    var attributes: MutableMap<String, String>,
    var status: SeasonalSwapExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalSwapExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalSwapExceptionStatus): Boolean {
        when (status) {
            SeasonalSwapExceptionStatus.PLANNED -> {
                when (next) {
                    SeasonalSwapExceptionStatus.COMPLETED -> return true
                }
                return false
            }
            SeasonalSwapExceptionStatus.SCHEDULED -> return next == SeasonalSwapExceptionStatus.IN_PROGRESS || next == SeasonalSwapExceptionStatus.COMPLETED
            SeasonalSwapExceptionStatus.IN_PROGRESS -> {
                when (next) {
                    SeasonalSwapExceptionStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalSwapExceptionStatus.COMPLETED -> {
                when (next) {
                    SeasonalSwapExceptionStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalSwapExceptionStatus) {
        if (!canMoveTo(next)) {
            error("seasonal swap exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = floorValue

    fun snapshot(): SeasonalSwapException {
        return SeasonalSwapException(
            code = code,
            label = label,
            floorValue = floorValue,
            checksum = checksum,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal swap exception in status planned. */
fun newSeasonalSwapException(code: String, label: String): SeasonalSwapException {
    return SeasonalSwapException(
        code = code,
        label = label,
        floorValue = 0.0,
        checksum = "",
        attributes = mutableMapOf<String, String>(),
        status = SeasonalSwapExceptionStatus.PLANNED,
    )
}
