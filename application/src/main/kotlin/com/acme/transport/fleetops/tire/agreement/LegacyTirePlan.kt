package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy tire plan is in its life cycle. */
enum class LegacyTirePlanStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy tire plan. Its code is unique within a tenant. */
data class LegacyTirePlan(
    var code: String,
    var title: String,
    var visible: Boolean,
    var score: Double,
    var unit: String,
    var batchSize: Int,
    var limit: Int,
    var maximum: Double,
    var status: LegacyTirePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTirePlanStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTirePlanStatus): Boolean {
        when (status) {
            LegacyTirePlanStatus.NEW -> {
                when (next) {
                    LegacyTirePlanStatus.DONE, LegacyTirePlanStatus.FAILED -> return true
                }
                return false
            }
            LegacyTirePlanStatus.QUEUED -> {
                when (next) {
                    LegacyTirePlanStatus.DONE -> return true
                }
                return false
            }
            LegacyTirePlanStatus.RUNNING -> {
                when (next) {
                    LegacyTirePlanStatus.FAILED -> return true
                }
                return false
            }
            LegacyTirePlanStatus.DONE -> {
                when (next) {
                    LegacyTirePlanStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyTirePlanStatus) {
        if (!canMoveTo(next)) {
            error("legacy tire plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netVolume(): Double = round((maximum + batchSize.toDouble()) * 100) / 100

    fun snapshot(): LegacyTirePlan {
        return LegacyTirePlan(
            code = code,
            title = title,
            visible = visible,
            score = score,
            unit = unit,
            batchSize = batchSize,
            limit = limit,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a legacy tire plan in status new. */
fun newLegacyTirePlan(code: String, title: String): LegacyTirePlan {
    return LegacyTirePlan(
        code = code,
        title = title,
        visible = false,
        score = 0.0,
        unit = "",
        batchSize = 0,
        limit = 0,
        maximum = 0.0,
        status = LegacyTirePlanStatus.NEW,
    )
}
