package com.acme.transport.emissions.evcharge.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge mapping is in its life cycle. */
enum class EvChargeMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A ev charge mapping as the ev charge team keeps it. */
data class EvChargeMapping(
    var code: String,
    var name: String,
    var minimum: Double,
    var revision: Int,
    var startsAt: Int,
    var status: EvChargeMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeMappingStatus): Boolean {
        when (status) {
            EvChargeMappingStatus.OPEN -> {
                when (next) {
                    EvChargeMappingStatus.IN_REVIEW -> return true
                }
                return false
            }
            EvChargeMappingStatus.IN_REVIEW -> {
                when (next) {
                    EvChargeMappingStatus.RESOLVED, EvChargeMappingStatus.REOPENED -> return true
                }
                return false
            }
            EvChargeMappingStatus.RESOLVED -> return next == EvChargeMappingStatus.REOPENED
            EvChargeMappingStatus.REOPENED -> {
                when (next) {
                    EvChargeMappingStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeMappingStatus) {
        if (!canMoveTo(next)) {
            error("ev charge mapping $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun effectiveValue(): Double = round((revision.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): EvChargeMapping {
        return EvChargeMapping(
            code = code,
            name = name,
            minimum = minimum,
            revision = revision,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a ev charge mapping in status open. */
fun newEvChargeMapping(code: String, name: String): EvChargeMapping {
    return EvChargeMapping(
        code = code,
        name = name,
        minimum = 0.0,
        revision = 0,
        startsAt = 0,
        status = EvChargeMappingStatus.OPEN,
    )
}
