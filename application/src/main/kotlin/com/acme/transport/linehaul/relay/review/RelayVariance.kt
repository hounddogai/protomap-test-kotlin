package com.acme.transport.linehaul.relay.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay variance is in its life cycle. */
enum class RelayVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A relay variance as the relay team keeps it. */
data class RelayVariance(
    var code: String,
    var name: String,
    var rate: Double,
    var description: String,
    var floorValue: Double,
    var status: RelayVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayVarianceStatus): Boolean {
        when (status) {
            RelayVarianceStatus.OPEN -> {
                when (next) {
                    RelayVarianceStatus.REOPENED -> return true
                }
                return false
            }
            RelayVarianceStatus.IN_REVIEW -> {
                when (next) {
                    RelayVarianceStatus.RESOLVED -> return true
                }
                return false
            }
            RelayVarianceStatus.RESOLVED -> {
                when (next) {
                    RelayVarianceStatus.REOPENED, RelayVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
            RelayVarianceStatus.REOPENED -> {
                when (next) {
                    RelayVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayVarianceStatus) {
        if (!canMoveTo(next)) {
            error("relay variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"relay variance"} $code"

    fun effectiveTotal(): Double = round(floorValue * rate * 100) / 100

    fun snapshot(): RelayVariance {
        return RelayVariance(
            code = code,
            name = name,
            rate = rate,
            description = description,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a relay variance in status open. */
fun newRelayVariance(code: String, name: String): RelayVariance {
    return RelayVariance(
        code = code,
        name = name,
        rate = 0.0,
        description = "",
        floorValue = 0.0,
        status = RelayVarianceStatus.OPEN,
    )
}
