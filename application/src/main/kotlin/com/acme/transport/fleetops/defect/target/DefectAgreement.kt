package com.acme.transport.fleetops.defect.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect agreement is in its life cycle. */
enum class DefectAgreementStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A defect agreement. Its code is unique within a tenant. */
data class DefectAgreement(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var endsAt: Int,
    var batchSize: Int,
    var minimum: Double,
    var score: Double,
    var carrier: String,
    var fuelLiters: Double,
    var capacity: Int,
    var status: DefectAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectAgreementStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectAgreementStatus): Boolean {
        when (status) {
            DefectAgreementStatus.PROPOSED -> {
                when (next) {
                    DefectAgreementStatus.REJECTED, DefectAgreementStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefectAgreementStatus.APPROVED -> return next == DefectAgreementStatus.REJECTED
            DefectAgreementStatus.REJECTED -> {
                when (next) {
                    DefectAgreementStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectAgreementStatus) {
        if (!canMoveTo(next)) {
            error("defect agreement $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedVolume(): Double = round((endsAt.toDouble() - score) * 100) / 100

    fun snapshot(): DefectAgreement {
        return DefectAgreement(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            endsAt = endsAt,
            batchSize = batchSize,
            minimum = minimum,
            score = score,
            carrier = carrier,
            fuelLiters = fuelLiters,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a defect agreement in status proposed. */
fun newDefectAgreement(code: String, label: String): DefectAgreement {
    return DefectAgreement(
        code = code,
        label = label,
        etaMinutes = 0,
        endsAt = 0,
        batchSize = 0,
        minimum = 0.0,
        score = 0.0,
        carrier = "",
        fuelLiters = 0.0,
        capacity = 0,
        status = DefectAgreementStatus.PROPOSED,
    )
}
