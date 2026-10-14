package com.acme.transport.linehaul.laneplan.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped lane plan policy is in its life cycle. */
enum class CappedLanePlanPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped lane plan policy. Its code is unique within a tenant. */
data class CappedLanePlanPolicy(
    var code: String,
    var name: String,
    var summary: String,
    var capacity: Int,
    var loadKg: Double,
    var status: CappedLanePlanPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedLanePlanPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedLanePlanPolicyStatus): Boolean {
        when (status) {
            CappedLanePlanPolicyStatus.REQUESTED -> {
                when (next) {
                    CappedLanePlanPolicyStatus.GRANTED -> return true
                }
                return false
            }
            CappedLanePlanPolicyStatus.GRANTED -> {
                when (next) {
                    CappedLanePlanPolicyStatus.EXPIRED -> return true
                }
                return false
            }
            CappedLanePlanPolicyStatus.REVOKED -> return next == CappedLanePlanPolicyStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedLanePlanPolicyStatus) {
        if (!canMoveTo(next)) {
            error("capped lane plan policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped lane plan policy"} $code"

    fun adjustedLoad(): Double = round((capacity.toDouble() + loadKg) * 100) / 100

    fun snapshot(): CappedLanePlanPolicy {
        return CappedLanePlanPolicy(
            code = code,
            name = name,
            summary = summary,
            capacity = capacity,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped lane plan policy in status requested. */
fun newCappedLanePlanPolicy(code: String, name: String): CappedLanePlanPolicy {
    return CappedLanePlanPolicy(
        code = code,
        name = name,
        summary = "",
        capacity = 0,
        loadKg = 0.0,
        status = CappedLanePlanPolicyStatus.REQUESTED,
    )
}
