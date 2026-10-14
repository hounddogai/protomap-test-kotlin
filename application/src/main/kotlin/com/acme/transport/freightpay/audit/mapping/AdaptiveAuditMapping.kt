package com.acme.transport.freightpay.audit.mapping

import com.acme.transport.kit.text.normalize

/** Where a adaptive audit mapping is in its life cycle. */
enum class AdaptiveAuditMappingStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A adaptive audit mapping. Its code is unique within a tenant. */
data class AdaptiveAuditMapping(
    var code: String,
    var label: String,
    var ownerId: String,
    var endsAt: Int,
    var currency: String,
    var visible: Boolean,
    var vehicleId: String,
    var status: AdaptiveAuditMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAuditMappingStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAuditMappingStatus): Boolean {
        when (status) {
            AdaptiveAuditMappingStatus.PROPOSED -> return next == AdaptiveAuditMappingStatus.APPROVED || next == AdaptiveAuditMappingStatus.REJECTED
            AdaptiveAuditMappingStatus.APPROVED -> {
                when (next) {
                    AdaptiveAuditMappingStatus.REJECTED, AdaptiveAuditMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
            AdaptiveAuditMappingStatus.REJECTED -> {
                when (next) {
                    AdaptiveAuditMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveAuditMappingStatus) {
        if (!canMoveTo(next)) {
            error("adaptive audit mapping $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): AdaptiveAuditMapping {
        return AdaptiveAuditMapping(
            code = code,
            label = label,
            ownerId = ownerId,
            endsAt = endsAt,
            currency = currency,
            visible = visible,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive audit mapping in status proposed. */
fun newAdaptiveAuditMapping(code: String, label: String): AdaptiveAuditMapping {
    return AdaptiveAuditMapping(
        code = code,
        label = label,
        ownerId = "",
        endsAt = 0,
        currency = "",
        visible = false,
        vehicleId = "",
        status = AdaptiveAuditMappingStatus.PROPOSED,
    )
}
