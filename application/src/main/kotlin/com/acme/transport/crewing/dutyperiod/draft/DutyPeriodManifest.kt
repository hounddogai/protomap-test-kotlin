package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.kit.text.normalize

/** Where a duty period manifest is in its life cycle. */
enum class DutyPeriodManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a duty period manifest and its current status. */
data class DutyPeriodManifest(
    var code: String,
    var name: String,
    var shiftCode: String,
    var segment: String,
    var carrier: String,
    var quantity: Int,
    var status: DutyPeriodManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodManifestStatus): Boolean {
        when (status) {
            DutyPeriodManifestStatus.NEW -> return next == DutyPeriodManifestStatus.FAILED
            DutyPeriodManifestStatus.QUEUED -> return next == DutyPeriodManifestStatus.DONE
            DutyPeriodManifestStatus.RUNNING -> return next == DutyPeriodManifestStatus.DONE || next == DutyPeriodManifestStatus.FAILED
            DutyPeriodManifestStatus.DONE -> {
                when (next) {
                    DutyPeriodManifestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodManifestStatus) {
        if (!canMoveTo(next)) {
            error("duty period manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"duty period manifest"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): DutyPeriodManifest {
        return DutyPeriodManifest(
            code = code,
            name = name,
            shiftCode = shiftCode,
            segment = segment,
            carrier = carrier,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a duty period manifest in status new. */
fun newDutyPeriodManifest(code: String, name: String): DutyPeriodManifest {
    return DutyPeriodManifest(
        code = code,
        name = name,
        shiftCode = "",
        segment = "",
        carrier = "",
        quantity = 0,
        status = DutyPeriodManifestStatus.NEW,
    )
}
