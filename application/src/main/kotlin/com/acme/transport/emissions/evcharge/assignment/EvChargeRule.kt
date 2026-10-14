package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge rule is in its life cycle. */
enum class EvChargeRuleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a ev charge rule and its current status. */
data class EvChargeRule(
    var code: String,
    var name: String,
    var active: Boolean,
    var amount: Double,
    var retries: Int,
    var lane: String,
    var tags: MutableList<String>,
    var floorValue: Double,
    var vehicleId: String,
    var status: EvChargeRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeRuleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeRuleStatus): Boolean {
        when (status) {
            EvChargeRuleStatus.NEW -> {
                when (next) {
                    EvChargeRuleStatus.DONE -> return true
                }
                return false
            }
            EvChargeRuleStatus.QUEUED -> {
                when (next) {
                    EvChargeRuleStatus.RUNNING, EvChargeRuleStatus.DONE -> return true
                }
                return false
            }
            EvChargeRuleStatus.RUNNING -> {
                when (next) {
                    EvChargeRuleStatus.FAILED -> return true
                }
                return false
            }
            EvChargeRuleStatus.DONE -> {
                when (next) {
                    EvChargeRuleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeRuleStatus) {
        if (!canMoveTo(next)) {
            error("ev charge rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"ev charge rule"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveValue(): Double = round(amount * retries.toDouble() * 100) / 100

    fun snapshot(): EvChargeRule {
        return EvChargeRule(
            code = code,
            name = name,
            active = active,
            amount = amount,
            retries = retries,
            lane = lane,
            tags = tags,
            floorValue = floorValue,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a ev charge rule in status new. */
fun newEvChargeRule(code: String, name: String): EvChargeRule {
    return EvChargeRule(
        code = code,
        name = name,
        active = false,
        amount = 0.0,
        retries = 0,
        lane = "",
        tags = mutableListOf<String>(),
        floorValue = 0.0,
        vehicleId = "",
        status = EvChargeRuleStatus.NEW,
    )
}
