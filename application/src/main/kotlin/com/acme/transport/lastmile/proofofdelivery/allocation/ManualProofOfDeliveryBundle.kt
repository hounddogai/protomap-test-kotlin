package com.acme.transport.lastmile.proofofdelivery.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual proof of delivery bundle is in its life cycle. */
enum class ManualProofOfDeliveryBundleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A manual proof of delivery bundle. Its code is unique within a tenant. */
data class ManualProofOfDeliveryBundle(
    var code: String,
    var label: String,
    var externalRef: String,
    var carrier: String,
    var trailerId: String,
    var maximum: Double,
    var weight: Double,
    var status: ManualProofOfDeliveryBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualProofOfDeliveryBundleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualProofOfDeliveryBundleStatus): Boolean {
        when (status) {
            ManualProofOfDeliveryBundleStatus.DRAFT -> return next == ManualProofOfDeliveryBundleStatus.SUSPENDED || next == ManualProofOfDeliveryBundleStatus.CLOSED
            ManualProofOfDeliveryBundleStatus.PENDING -> {
                when (next) {
                    ManualProofOfDeliveryBundleStatus.ACTIVE, ManualProofOfDeliveryBundleStatus.SUSPENDED -> return true
                }
                return false
            }
            ManualProofOfDeliveryBundleStatus.ACTIVE -> return next == ManualProofOfDeliveryBundleStatus.CLOSED
            ManualProofOfDeliveryBundleStatus.SUSPENDED -> {
                when (next) {
                    ManualProofOfDeliveryBundleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualProofOfDeliveryBundleStatus) {
        if (!canMoveTo(next)) {
            error("manual proof of delivery bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netTotal(): Double = round(weight * maximum * 100) / 100

    fun snapshot(): ManualProofOfDeliveryBundle {
        return ManualProofOfDeliveryBundle(
            code = code,
            label = label,
            externalRef = externalRef,
            carrier = carrier,
            trailerId = trailerId,
            maximum = maximum,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual proof of delivery bundle in status draft. */
fun newManualProofOfDeliveryBundle(code: String, label: String): ManualProofOfDeliveryBundle {
    return ManualProofOfDeliveryBundle(
        code = code,
        label = label,
        externalRef = "",
        carrier = "",
        trailerId = "",
        maximum = 0.0,
        weight = 0.0,
        status = ManualProofOfDeliveryBundleStatus.DRAFT,
    )
}
