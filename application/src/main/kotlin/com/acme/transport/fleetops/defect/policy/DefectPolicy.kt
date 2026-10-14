package com.acme.transport.fleetops.defect.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The defect policy records of fleetops's defect work, and the rules, stores, and calculations around them.

/** Where a defect policy is in its life cycle. */
enum class DefectPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A defect policy. Its code is unique within a tenant. */
data class DefectPolicy(
    var code: String,
    var title: String,
    var amount: Double,
    var weight: Double,
    var routeCode: String,
    var currency: String,
    var minimum: Double,
    var loadKg: Double,
    var threshold: Double,
    var score: Double,
    var status: DefectPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectPolicyStatus): Boolean {
        when (status) {
            DefectPolicyStatus.REQUESTED -> return next == DefectPolicyStatus.REVOKED
            DefectPolicyStatus.GRANTED -> {
                when (next) {
                    DefectPolicyStatus.REVOKED -> return true
                }
                return false
            }
            DefectPolicyStatus.REVOKED -> return next == DefectPolicyStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefectPolicyStatus) {
        if (!canMoveTo(next)) {
            error("defect policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netValue(): Double = round((amount + threshold) * 100) / 100

    fun snapshot(): DefectPolicy {
        return DefectPolicy(
            code = code,
            title = title,
            amount = amount,
            weight = weight,
            routeCode = routeCode,
            currency = currency,
            minimum = minimum,
            loadKg = loadKg,
            threshold = threshold,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a defect policy in status requested. */
fun newDefectPolicy(code: String, title: String): DefectPolicy {
    return DefectPolicy(
        code = code,
        title = title,
        amount = 0.0,
        weight = 0.0,
        routeCode = "",
        currency = "",
        minimum = 0.0,
        loadKg = 0.0,
        threshold = 0.0,
        score = 0.0,
        status = DefectPolicyStatus.REQUESTED,
    )
}
