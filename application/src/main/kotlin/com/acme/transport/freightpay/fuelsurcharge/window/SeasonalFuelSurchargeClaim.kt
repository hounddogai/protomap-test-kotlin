package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.kit.text.normalize

/** Where a seasonal fuel surcharge claim is in its life cycle. */
enum class SeasonalFuelSurchargeClaimStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal fuel surcharge claim. Its code is unique within a tenant. */
data class SeasonalFuelSurchargeClaim(
    var code: String,
    var label: String,
    var checksum: String,
    var sequence: Int,
    var ownerId: String,
    var status: SeasonalFuelSurchargeClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalFuelSurchargeClaimStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalFuelSurchargeClaimStatus): Boolean {
        when (status) {
            SeasonalFuelSurchargeClaimStatus.REQUESTED -> {
                when (next) {
                    SeasonalFuelSurchargeClaimStatus.GRANTED, SeasonalFuelSurchargeClaimStatus.REVOKED -> return true
                }
                return false
            }
            SeasonalFuelSurchargeClaimStatus.GRANTED -> {
                when (next) {
                    SeasonalFuelSurchargeClaimStatus.REVOKED, SeasonalFuelSurchargeClaimStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalFuelSurchargeClaimStatus.REVOKED -> {
                when (next) {
                    SeasonalFuelSurchargeClaimStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalFuelSurchargeClaimStatus) {
        if (!canMoveTo(next)) {
            error("seasonal fuel surcharge claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal fuel surcharge claim"} $code"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): SeasonalFuelSurchargeClaim {
        return SeasonalFuelSurchargeClaim(
            code = code,
            label = label,
            checksum = checksum,
            sequence = sequence,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal fuel surcharge claim in status requested. */
fun newSeasonalFuelSurchargeClaim(code: String, label: String): SeasonalFuelSurchargeClaim {
    return SeasonalFuelSurchargeClaim(
        code = code,
        label = label,
        checksum = "",
        sequence = 0,
        ownerId = "",
        status = SeasonalFuelSurchargeClaimStatus.REQUESTED,
    )
}
