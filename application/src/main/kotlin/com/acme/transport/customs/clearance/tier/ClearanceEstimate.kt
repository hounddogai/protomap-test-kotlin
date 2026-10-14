package com.acme.transport.customs.clearance.tier

import com.acme.transport.kit.text.normalize

/** Where a clearance estimate is in its life cycle. */
enum class ClearanceEstimateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a clearance estimate and its current status. */
data class ClearanceEstimate(
    var code: String,
    var label: String,
    var shiftCode: String,
    var attributes: MutableMap<String, String>,
    var etaMinutes: Int,
    var status: ClearanceEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceEstimateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceEstimateStatus): Boolean {
        when (status) {
            ClearanceEstimateStatus.REQUESTED -> return next == ClearanceEstimateStatus.EXPIRED
            ClearanceEstimateStatus.GRANTED -> {
                when (next) {
                    ClearanceEstimateStatus.REVOKED, ClearanceEstimateStatus.EXPIRED -> return true
                }
                return false
            }
            ClearanceEstimateStatus.REVOKED -> return next == ClearanceEstimateStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ClearanceEstimateStatus) {
        if (!canMoveTo(next)) {
            error("clearance estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"clearance estimate"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): ClearanceEstimate {
        return ClearanceEstimate(
            code = code,
            label = label,
            shiftCode = shiftCode,
            attributes = attributes,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a clearance estimate in status requested. */
fun newClearanceEstimate(code: String, label: String): ClearanceEstimate {
    return ClearanceEstimate(
        code = code,
        label = label,
        shiftCode = "",
        attributes = mutableMapOf<String, String>(),
        etaMinutes = 0,
        status = ClearanceEstimateStatus.REQUESTED,
    )
}
