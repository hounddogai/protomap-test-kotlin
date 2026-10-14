package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss policy is in its life cycle. */
enum class NearMissPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a near miss policy and its current status. */
data class NearMissPolicy(
    var code: String,
    var name: String,
    var score: Double,
    var unit: String,
    var driverId: String,
    var limit: Int,
    var status: NearMissPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissPolicyStatus): Boolean {
        when (status) {
            NearMissPolicyStatus.CREATED -> {
                when (next) {
                    NearMissPolicyStatus.PUBLISHED -> return true
                }
                return false
            }
            NearMissPolicyStatus.VALIDATED -> {
                when (next) {
                    NearMissPolicyStatus.RETIRED -> return true
                }
                return false
            }
            NearMissPolicyStatus.PUBLISHED -> {
                when (next) {
                    NearMissPolicyStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NearMissPolicyStatus) {
        if (!canMoveTo(next)) {
            error("near miss policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netExposure(): Double = round((score + limit.toDouble()) * 100) / 100

    fun snapshot(): NearMissPolicy {
        return NearMissPolicy(
            code = code,
            name = name,
            score = score,
            unit = unit,
            driverId = driverId,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a near miss policy in status created. */
fun newNearMissPolicy(code: String, name: String): NearMissPolicy {
    return NearMissPolicy(
        code = code,
        name = name,
        score = 0.0,
        unit = "",
        driverId = "",
        limit = 0,
        status = NearMissPolicyStatus.CREATED,
    )
}
