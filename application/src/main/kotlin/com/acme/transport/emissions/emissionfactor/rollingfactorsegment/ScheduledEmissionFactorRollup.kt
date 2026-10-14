package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled emission factor rollup is in its life cycle. */
enum class ScheduledEmissionFactorRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A scheduled emission factor rollup. Its code is unique within a tenant. */
data class ScheduledEmissionFactorRollup(
    var code: String,
    var title: String,
    var margin: Double,
    var createdAt: Int,
    var maximum: Double,
    var rate: Double,
    var active: Boolean,
    var minimum: Double,
    var status: ScheduledEmissionFactorRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledEmissionFactorRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledEmissionFactorRollupStatus): Boolean {
        when (status) {
            ScheduledEmissionFactorRollupStatus.REQUESTED -> {
                when (next) {
                    ScheduledEmissionFactorRollupStatus.GRANTED, ScheduledEmissionFactorRollupStatus.EXPIRED -> return true
                }
                return false
            }
            ScheduledEmissionFactorRollupStatus.GRANTED -> {
                when (next) {
                    ScheduledEmissionFactorRollupStatus.REVOKED, ScheduledEmissionFactorRollupStatus.EXPIRED -> return true
                }
                return false
            }
            ScheduledEmissionFactorRollupStatus.REVOKED -> return next == ScheduledEmissionFactorRollupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ScheduledEmissionFactorRollupStatus) {
        if (!canMoveTo(next)) {
            error("scheduled emission factor rollup $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedTotal(): Double = round((rate + createdAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledEmissionFactorRollup {
        return ScheduledEmissionFactorRollup(
            code = code,
            title = title,
            margin = margin,
            createdAt = createdAt,
            maximum = maximum,
            rate = rate,
            active = active,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled emission factor rollup in status requested. */
fun newScheduledEmissionFactorRollup(code: String, title: String): ScheduledEmissionFactorRollup {
    return ScheduledEmissionFactorRollup(
        code = code,
        title = title,
        margin = 0.0,
        createdAt = 0,
        maximum = 0.0,
        rate = 0.0,
        active = false,
        minimum = 0.0,
        status = ScheduledEmissionFactorRollupStatus.REQUESTED,
    )
}
