package com.acme.transport.network.transittime.slot

import com.acme.transport.kit.text.normalize

/** Where a transit time template is in its life cycle. */
enum class TransitTimeTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A transit time template. Its code is unique within a tenant. */
data class TransitTimeTemplate(
    var code: String,
    var name: String,
    var description: String,
    var floorValue: Double,
    var shiftCode: String,
    var region: String,
    var status: TransitTimeTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeTemplateStatus): Boolean {
        when (status) {
            TransitTimeTemplateStatus.PLANNED -> {
                when (next) {
                    TransitTimeTemplateStatus.SCHEDULED, TransitTimeTemplateStatus.CANCELLED -> return true
                }
                return false
            }
            TransitTimeTemplateStatus.SCHEDULED -> return next == TransitTimeTemplateStatus.IN_PROGRESS || next == TransitTimeTemplateStatus.CANCELLED
            TransitTimeTemplateStatus.IN_PROGRESS -> {
                when (next) {
                    TransitTimeTemplateStatus.COMPLETED -> return true
                }
                return false
            }
            TransitTimeTemplateStatus.COMPLETED -> {
                when (next) {
                    TransitTimeTemplateStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeTemplateStatus) {
        if (!canMoveTo(next)) {
            error("transit time template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = floorValue

    fun snapshot(): TransitTimeTemplate {
        return TransitTimeTemplate(
            code = code,
            name = name,
            description = description,
            floorValue = floorValue,
            shiftCode = shiftCode,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a transit time template in status planned. */
fun newTransitTimeTemplate(code: String, name: String): TransitTimeTemplate {
    return TransitTimeTemplate(
        code = code,
        name = name,
        description = "",
        floorValue = 0.0,
        shiftCode = "",
        region = "",
        status = TransitTimeTemplateStatus.PLANNED,
    )
}
