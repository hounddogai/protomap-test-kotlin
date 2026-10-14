package com.acme.transport.lastmile.parcel.variance

import com.acme.transport.kit.text.normalize

// The parcel variance records of lastmile's parcel work, and the rules, stores, and calculations around them.

/** Where a parcel variance is in its life cycle. */
enum class ParcelVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a parcel variance and its current status. */
data class ParcelVariance(
    var code: String,
    var name: String,
    var checksum: String,
    var phone: String,
    var vehicleId: String,
    var externalRef: String,
    var amount: Double,
    var visible: Boolean,
    var segment: String,
    var status: ParcelVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelVarianceStatus): Boolean {
        when (status) {
            ParcelVarianceStatus.PROPOSED -> return next == ParcelVarianceStatus.APPROVED || next == ParcelVarianceStatus.REJECTED
            ParcelVarianceStatus.APPROVED -> {
                when (next) {
                    ParcelVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
            ParcelVarianceStatus.REJECTED -> {
                when (next) {
                    ParcelVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelVarianceStatus) {
        if (!canMoveTo(next)) {
            error("parcel variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = amount

    fun snapshot(): ParcelVariance {
        return ParcelVariance(
            code = code,
            name = name,
            checksum = checksum,
            phone = phone,
            vehicleId = vehicleId,
            externalRef = externalRef,
            amount = amount,
            visible = visible,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a parcel variance in status proposed. */
fun newParcelVariance(code: String, name: String): ParcelVariance {
    return ParcelVariance(
        code = code,
        name = name,
        checksum = "",
        phone = "",
        vehicleId = "",
        externalRef = "",
        amount = 0.0,
        visible = false,
        segment = "",
        status = ParcelVarianceStatus.PROPOSED,
    )
}
