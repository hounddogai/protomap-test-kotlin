package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered proof of delivery manifest records of lastmile's proof of delivery work, and the rules, stores,
// and calculations around them.

/** Where a tiered proof of delivery manifest is in its life cycle. */
enum class TieredProofOfDeliveryManifestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a tiered proof of delivery manifest and its current status. */
data class TieredProofOfDeliveryManifest(
    var code: String,
    var name: String,
    var shiftCode: String,
    var currency: String,
    var durationMinutes: Int,
    var endsAt: Int,
    var status: TieredProofOfDeliveryManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredProofOfDeliveryManifestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredProofOfDeliveryManifestStatus): Boolean {
        when (status) {
            TieredProofOfDeliveryManifestStatus.DRAFT -> {
                when (next) {
                    TieredProofOfDeliveryManifestStatus.SUSPENDED -> return true
                }
                return false
            }
            TieredProofOfDeliveryManifestStatus.PENDING -> {
                when (next) {
                    TieredProofOfDeliveryManifestStatus.SUSPENDED -> return true
                }
                return false
            }
            TieredProofOfDeliveryManifestStatus.ACTIVE -> {
                when (next) {
                    TieredProofOfDeliveryManifestStatus.SUSPENDED -> return true
                }
                return false
            }
            TieredProofOfDeliveryManifestStatus.SUSPENDED -> {
                when (next) {
                    TieredProofOfDeliveryManifestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredProofOfDeliveryManifestStatus) {
        if (!canMoveTo(next)) {
            error("tiered proof of delivery manifest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveValue(): Double = round(durationMinutes.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): TieredProofOfDeliveryManifest {
        return TieredProofOfDeliveryManifest(
            code = code,
            name = name,
            shiftCode = shiftCode,
            currency = currency,
            durationMinutes = durationMinutes,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered proof of delivery manifest in status draft. */
fun newTieredProofOfDeliveryManifest(code: String, name: String): TieredProofOfDeliveryManifest {
    return TieredProofOfDeliveryManifest(
        code = code,
        name = name,
        shiftCode = "",
        currency = "",
        durationMinutes = 0,
        endsAt = 0,
        status = TieredProofOfDeliveryManifestStatus.DRAFT,
    )
}
