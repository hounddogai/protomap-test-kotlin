package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global duty period rollup is in its life cycle. */
enum class GlobalDutyPeriodRollupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a global duty period rollup and its current status. */
data class GlobalDutyPeriodRollup(
    var code: String,
    var label: String,
    var quantity: Int,
    var score: Double,
    var locale: String,
    var unit: String,
    var depot: String,
    var weight: Double,
    var ceiling: Double,
    var status: GlobalDutyPeriodRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDutyPeriodRollupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDutyPeriodRollupStatus): Boolean {
        when (status) {
            GlobalDutyPeriodRollupStatus.NEW -> {
                when (next) {
                    GlobalDutyPeriodRollupStatus.QUEUED, GlobalDutyPeriodRollupStatus.DONE -> return true
                }
                return false
            }
            GlobalDutyPeriodRollupStatus.QUEUED -> {
                when (next) {
                    GlobalDutyPeriodRollupStatus.RUNNING, GlobalDutyPeriodRollupStatus.DONE -> return true
                }
                return false
            }
            GlobalDutyPeriodRollupStatus.RUNNING -> {
                when (next) {
                    GlobalDutyPeriodRollupStatus.DONE, GlobalDutyPeriodRollupStatus.FAILED -> return true
                }
                return false
            }
            GlobalDutyPeriodRollupStatus.DONE -> return next == GlobalDutyPeriodRollupStatus.FAILED
        }
        return false
    }

    fun moveTo(next: GlobalDutyPeriodRollupStatus) {
        if (!canMoveTo(next)) {
            error("global duty period rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global duty period rollup"} $code"

    fun weightedVolume(): Double = round(ceiling * quantity.toDouble() * 100) / 100

    fun snapshot(): GlobalDutyPeriodRollup {
        return GlobalDutyPeriodRollup(
            code = code,
            label = label,
            quantity = quantity,
            score = score,
            locale = locale,
            unit = unit,
            depot = depot,
            weight = weight,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a global duty period rollup in status new. */
fun newGlobalDutyPeriodRollup(code: String, label: String): GlobalDutyPeriodRollup {
    return GlobalDutyPeriodRollup(
        code = code,
        label = label,
        quantity = 0,
        score = 0.0,
        locale = "",
        unit = "",
        depot = "",
        weight = 0.0,
        ceiling = 0.0,
        status = GlobalDutyPeriodRollupStatus.NEW,
    )
}
