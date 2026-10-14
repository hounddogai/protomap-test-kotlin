package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default proof of delivery manifest is in its life cycle. */
enum class DefaultProofOfDeliveryManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A default proof of delivery manifest. Its code is unique within a tenant. */
data class DefaultProofOfDeliveryManifest(
    var code: String,
    var label: String,
    var priority: Int,
    var quantity: Int,
    var weight: Double,
    var shiftCode: String,
    var cost: Double,
    var graceDays: Int,
    var summary: String,
    var unit: String,
    var stopCount: Int,
    var status: DefaultProofOfDeliveryManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultProofOfDeliveryManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultProofOfDeliveryManifestStatus): Boolean {
        when (status) {
            DefaultProofOfDeliveryManifestStatus.PROPOSED -> {
                when (next) {
                    DefaultProofOfDeliveryManifestStatus.APPROVED, DefaultProofOfDeliveryManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefaultProofOfDeliveryManifestStatus.APPROVED -> {
                when (next) {
                    DefaultProofOfDeliveryManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefaultProofOfDeliveryManifestStatus.REJECTED -> return next == DefaultProofOfDeliveryManifestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DefaultProofOfDeliveryManifestStatus) {
        if (!canMoveTo(next)) {
            error("default proof of delivery manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedVolume(): Double = round(graceDays.toDouble() * cost * 100) / 100

    fun snapshot(): DefaultProofOfDeliveryManifest {
        return DefaultProofOfDeliveryManifest(
            code = code,
            label = label,
            priority = priority,
            quantity = quantity,
            weight = weight,
            shiftCode = shiftCode,
            cost = cost,
            graceDays = graceDays,
            summary = summary,
            unit = unit,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a default proof of delivery manifest in status proposed. */
fun newDefaultProofOfDeliveryManifest(code: String, label: String): DefaultProofOfDeliveryManifest {
    return DefaultProofOfDeliveryManifest(
        code = code,
        label = label,
        priority = 0,
        quantity = 0,
        weight = 0.0,
        shiftCode = "",
        cost = 0.0,
        graceDays = 0,
        summary = "",
        unit = "",
        stopCount = 0,
        status = DefaultProofOfDeliveryManifestStatus.PROPOSED,
    )
}
