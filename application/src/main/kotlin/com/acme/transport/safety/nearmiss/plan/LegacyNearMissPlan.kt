package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.text.normalize

/** Where a legacy near miss plan is in its life cycle. */
enum class LegacyNearMissPlanStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a legacy near miss plan and its current status. */
data class LegacyNearMissPlan(
    var code: String,
    var label: String,
    var trailerId: String,
    var externalRef: String,
    var carrier: String,
    var quantity: Int,
    var status: LegacyNearMissPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyNearMissPlanStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyNearMissPlanStatus): Boolean {
        when (status) {
            LegacyNearMissPlanStatus.NEW -> {
                when (next) {
                    LegacyNearMissPlanStatus.QUEUED, LegacyNearMissPlanStatus.FAILED -> return true
                }
                return false
            }
            LegacyNearMissPlanStatus.QUEUED -> {
                when (next) {
                    LegacyNearMissPlanStatus.DONE -> return true
                }
                return false
            }
            LegacyNearMissPlanStatus.RUNNING -> {
                when (next) {
                    LegacyNearMissPlanStatus.DONE, LegacyNearMissPlanStatus.FAILED -> return true
                }
                return false
            }
            LegacyNearMissPlanStatus.DONE -> {
                when (next) {
                    LegacyNearMissPlanStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyNearMissPlanStatus) {
        if (!canMoveTo(next)) {
            error("legacy near miss plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): LegacyNearMissPlan {
        return LegacyNearMissPlan(
            code = code,
            label = label,
            trailerId = trailerId,
            externalRef = externalRef,
            carrier = carrier,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy near miss plan in status new. */
fun newLegacyNearMissPlan(code: String, label: String): LegacyNearMissPlan {
    return LegacyNearMissPlan(
        code = code,
        label = label,
        trailerId = "",
        externalRef = "",
        carrier = "",
        quantity = 0,
        status = LegacyNearMissPlanStatus.NEW,
    )
}
