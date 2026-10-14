package com.acme.transport.terminal.dwell.dwelltemplate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling dwell template records of terminal's dwell work, and the rules, stores, and calculations around
// them.

/** Where a rolling dwell template is in its life cycle. */
enum class RollingDwellTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a rolling dwell template and its current status. */
data class RollingDwellTemplate(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var score: Double,
    var endsAt: Int,
    var pinned: Boolean,
    var depot: String,
    var shiftCode: String,
    var category: String,
    var currency: String,
    var status: RollingDwellTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDwellTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDwellTemplateStatus): Boolean {
        when (status) {
            RollingDwellTemplateStatus.DRAFT -> return next == RollingDwellTemplateStatus.PENDING
            RollingDwellTemplateStatus.PENDING -> {
                when (next) {
                    RollingDwellTemplateStatus.SUSPENDED, RollingDwellTemplateStatus.CLOSED -> return true
                }
                return false
            }
            RollingDwellTemplateStatus.ACTIVE -> {
                when (next) {
                    RollingDwellTemplateStatus.CLOSED -> return true
                }
                return false
            }
            RollingDwellTemplateStatus.SUSPENDED -> {
                when (next) {
                    RollingDwellTemplateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingDwellTemplateStatus) {
        if (!canMoveTo(next)) {
            error("rolling dwell template $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"rolling dwell template"} $code"

    fun adjustedLoad(): Double = round((fuelLiters + score) * 100) / 100

    fun snapshot(): RollingDwellTemplate {
        return RollingDwellTemplate(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            score = score,
            endsAt = endsAt,
            pinned = pinned,
            depot = depot,
            shiftCode = shiftCode,
            category = category,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rolling dwell template in status draft. */
fun newRollingDwellTemplate(code: String, name: String): RollingDwellTemplate {
    return RollingDwellTemplate(
        code = code,
        name = name,
        fuelLiters = 0.0,
        score = 0.0,
        endsAt = 0,
        pinned = false,
        depot = "",
        shiftCode = "",
        category = "",
        currency = "",
        status = RollingDwellTemplateStatus.DRAFT,
    )
}
