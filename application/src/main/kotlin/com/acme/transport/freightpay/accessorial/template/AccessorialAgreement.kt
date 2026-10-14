package com.acme.transport.freightpay.accessorial.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial agreement is in its life cycle. */
enum class AccessorialAgreementStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a accessorial agreement and its current status. */
data class AccessorialAgreement(
    var code: String,
    var name: String,
    var batchSize: Int,
    var version: Int,
    var capacity: Int,
    var fuelLiters: Double,
    var dock: String,
    var lane: String,
    var minimum: Double,
    var description: String,
    var status: AccessorialAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialAgreementStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialAgreementStatus): Boolean {
        when (status) {
            AccessorialAgreementStatus.PROPOSED -> return next == AccessorialAgreementStatus.APPROVED || next == AccessorialAgreementStatus.REJECTED
            AccessorialAgreementStatus.APPROVED -> return next == AccessorialAgreementStatus.REJECTED || next == AccessorialAgreementStatus.WITHDRAWN
            AccessorialAgreementStatus.REJECTED -> return next == AccessorialAgreementStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AccessorialAgreementStatus) {
        if (!canMoveTo(next)) {
            error("accessorial agreement $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun effectiveVolume(): Double = round(fuelLiters * minimum * 100) / 100

    fun snapshot(): AccessorialAgreement {
        return AccessorialAgreement(
            code = code,
            name = name,
            batchSize = batchSize,
            version = version,
            capacity = capacity,
            fuelLiters = fuelLiters,
            dock = dock,
            lane = lane,
            minimum = minimum,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a accessorial agreement in status proposed. */
fun newAccessorialAgreement(code: String, name: String): AccessorialAgreement {
    return AccessorialAgreement(
        code = code,
        name = name,
        batchSize = 0,
        version = 0,
        capacity = 0,
        fuelLiters = 0.0,
        dock = "",
        lane = "",
        minimum = 0.0,
        description = "",
        status = AccessorialAgreementStatus.PROPOSED,
    )
}
