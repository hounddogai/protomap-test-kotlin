package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import com.acme.transport.kit.text.normalize

/** Where a manual proof of delivery mapping is in its life cycle. */
enum class ManualProofOfDeliveryMappingStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A manual proof of delivery mapping. Its code is unique within a tenant. */
data class ManualProofOfDeliveryMapping(
    var code: String,
    var label: String,
    var depot: String,
    var active: Boolean,
    var summary: String,
    var quantity: Int,
    var status: ManualProofOfDeliveryMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualProofOfDeliveryMappingStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualProofOfDeliveryMappingStatus): Boolean {
        when (status) {
            ManualProofOfDeliveryMappingStatus.DRAFT -> return next == ManualProofOfDeliveryMappingStatus.PENDING || next == ManualProofOfDeliveryMappingStatus.CLOSED
            ManualProofOfDeliveryMappingStatus.PENDING -> {
                when (next) {
                    ManualProofOfDeliveryMappingStatus.SUSPENDED -> return true
                }
                return false
            }
            ManualProofOfDeliveryMappingStatus.ACTIVE -> return next == ManualProofOfDeliveryMappingStatus.SUSPENDED
            ManualProofOfDeliveryMappingStatus.SUSPENDED -> {
                when (next) {
                    ManualProofOfDeliveryMappingStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualProofOfDeliveryMappingStatus) {
        if (!canMoveTo(next)) {
            error("manual proof of delivery mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): ManualProofOfDeliveryMapping {
        return ManualProofOfDeliveryMapping(
            code = code,
            label = label,
            depot = depot,
            active = active,
            summary = summary,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a manual proof of delivery mapping in status draft. */
fun newManualProofOfDeliveryMapping(code: String, label: String): ManualProofOfDeliveryMapping {
    return ManualProofOfDeliveryMapping(
        code = code,
        label = label,
        depot = "",
        active = false,
        summary = "",
        quantity = 0,
        status = ManualProofOfDeliveryMappingStatus.DRAFT,
    )
}
