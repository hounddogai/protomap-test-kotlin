package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.text.normalize

/** Where a capped lane plan threshold is in its life cycle. */
enum class CappedLanePlanThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capped lane plan threshold. Its code is unique within a tenant. */
data class CappedLanePlanThreshold(
    var code: String,
    var name: String,
    var shiftCode: String,
    var ratio: Double,
    var vehicleId: String,
    var phone: String,
    var status: CappedLanePlanThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedLanePlanThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedLanePlanThresholdStatus): Boolean {
        when (status) {
            CappedLanePlanThresholdStatus.NEW -> {
                when (next) {
                    CappedLanePlanThresholdStatus.DONE -> return true
                }
                return false
            }
            CappedLanePlanThresholdStatus.QUEUED -> return next == CappedLanePlanThresholdStatus.DONE || next == CappedLanePlanThresholdStatus.FAILED
            CappedLanePlanThresholdStatus.RUNNING -> {
                when (next) {
                    CappedLanePlanThresholdStatus.DONE, CappedLanePlanThresholdStatus.FAILED -> return true
                }
                return false
            }
            CappedLanePlanThresholdStatus.DONE -> {
                when (next) {
                    CappedLanePlanThresholdStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedLanePlanThresholdStatus) {
        if (!canMoveTo(next)) {
            error("capped lane plan threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = ratio

    fun snapshot(): CappedLanePlanThreshold {
        return CappedLanePlanThreshold(
            code = code,
            name = name,
            shiftCode = shiftCode,
            ratio = ratio,
            vehicleId = vehicleId,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped lane plan threshold in status new. */
fun newCappedLanePlanThreshold(code: String, name: String): CappedLanePlanThreshold {
    return CappedLanePlanThreshold(
        code = code,
        name = name,
        shiftCode = "",
        ratio = 0.0,
        vehicleId = "",
        phone = "",
        status = CappedLanePlanThresholdStatus.NEW,
    )
}
