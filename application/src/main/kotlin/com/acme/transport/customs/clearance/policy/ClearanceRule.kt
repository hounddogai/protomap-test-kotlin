package com.acme.transport.customs.clearance.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance rule is in its life cycle. */
enum class ClearanceRuleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a clearance rule and its current status. */
data class ClearanceRule(
    var code: String,
    var name: String,
    var attempts: Int,
    var floorValue: Double,
    var expiresAt: Int,
    var attributes: MutableMap<String, String>,
    var depot: String,
    var version: Int,
    var batchSize: Int,
    var notes: String,
    var status: ClearanceRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceRuleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceRuleStatus): Boolean {
        when (status) {
            ClearanceRuleStatus.PROPOSED -> return next == ClearanceRuleStatus.REJECTED || next == ClearanceRuleStatus.WITHDRAWN
            ClearanceRuleStatus.APPROVED -> return next == ClearanceRuleStatus.REJECTED || next == ClearanceRuleStatus.WITHDRAWN
            ClearanceRuleStatus.REJECTED -> {
                when (next) {
                    ClearanceRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceRuleStatus) {
        if (!canMoveTo(next)) {
            error("clearance rule $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedVolume(): Double = round((version.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): ClearanceRule {
        return ClearanceRule(
            code = code,
            name = name,
            attempts = attempts,
            floorValue = floorValue,
            expiresAt = expiresAt,
            attributes = attributes,
            depot = depot,
            version = version,
            batchSize = batchSize,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a clearance rule in status proposed. */
fun newClearanceRule(code: String, name: String): ClearanceRule {
    return ClearanceRule(
        code = code,
        name = name,
        attempts = 0,
        floorValue = 0.0,
        expiresAt = 0,
        attributes = mutableMapOf<String, String>(),
        depot = "",
        version = 0,
        batchSize = 0,
        notes = "",
        status = ClearanceRuleStatus.PROPOSED,
    )
}
