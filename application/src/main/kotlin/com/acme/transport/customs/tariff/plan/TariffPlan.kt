package com.acme.transport.customs.tariff.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tariff plan records of customs's tariff work, and the rules, stores, and calculations around them.

/** Where a tariff plan is in its life cycle. */
enum class TariffPlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tariff plan as the tariff team keeps it. */
data class TariffPlan(
    var code: String,
    var title: String,
    var capacity: Int,
    var region: String,
    var shiftCode: String,
    var endsAt: Int,
    var source: String,
    var status: TariffPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffPlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffPlanStatus): Boolean {
        when (status) {
            TariffPlanStatus.PLANNED -> {
                when (next) {
                    TariffPlanStatus.IN_PROGRESS, TariffPlanStatus.CANCELLED -> return true
                }
                return false
            }
            TariffPlanStatus.SCHEDULED -> {
                when (next) {
                    TariffPlanStatus.CANCELLED -> return true
                }
                return false
            }
            TariffPlanStatus.IN_PROGRESS -> return next == TariffPlanStatus.COMPLETED || next == TariffPlanStatus.CANCELLED
            TariffPlanStatus.COMPLETED -> {
                when (next) {
                    TariffPlanStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffPlanStatus) {
        if (!canMoveTo(next)) {
            error("tariff plan $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tariff plan"} $code"

    fun adjustedLoad(): Double = round((endsAt.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): TariffPlan {
        return TariffPlan(
            code = code,
            title = title,
            capacity = capacity,
            region = region,
            shiftCode = shiftCode,
            endsAt = endsAt,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a tariff plan in status planned. */
fun newTariffPlan(code: String, title: String): TariffPlan {
    return TariffPlan(
        code = code,
        title = title,
        capacity = 0,
        region = "",
        shiftCode = "",
        endsAt = 0,
        source = "",
        status = TariffPlanStatus.PLANNED,
    )
}
