package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted audit policy is in its life cycle. */
enum class WeightedAuditPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a weighted audit policy and its current status. */
data class WeightedAuditPolicy(
    var code: String,
    var name: String,
    var checksum: String,
    var attributes: MutableMap<String, String>,
    var margin: Double,
    var score: Double,
    var threshold: Double,
    var routeCode: String,
    var floorValue: Double,
    var status: WeightedAuditPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAuditPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAuditPolicyStatus): Boolean {
        when (status) {
            WeightedAuditPolicyStatus.NEW -> {
                when (next) {
                    WeightedAuditPolicyStatus.FAILED -> return true
                }
                return false
            }
            WeightedAuditPolicyStatus.QUEUED -> {
                when (next) {
                    WeightedAuditPolicyStatus.DONE -> return true
                }
                return false
            }
            WeightedAuditPolicyStatus.RUNNING -> return next == WeightedAuditPolicyStatus.DONE || next == WeightedAuditPolicyStatus.FAILED
            WeightedAuditPolicyStatus.DONE -> {
                when (next) {
                    WeightedAuditPolicyStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedAuditPolicyStatus) {
        if (!canMoveTo(next)) {
            error("weighted audit policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted audit policy"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round(score * margin * 100) / 100

    fun snapshot(): WeightedAuditPolicy {
        return WeightedAuditPolicy(
            code = code,
            name = name,
            checksum = checksum,
            attributes = attributes,
            margin = margin,
            score = score,
            threshold = threshold,
            routeCode = routeCode,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted audit policy in status new. */
fun newWeightedAuditPolicy(code: String, name: String): WeightedAuditPolicy {
    return WeightedAuditPolicy(
        code = code,
        name = name,
        checksum = "",
        attributes = mutableMapOf<String, String>(),
        margin = 0.0,
        score = 0.0,
        threshold = 0.0,
        routeCode = "",
        floorValue = 0.0,
        status = WeightedAuditPolicyStatus.NEW,
    )
}
