package com.acme.transport.emissions.evcharge.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge template is in its life cycle. */
enum class EvChargeTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A ev charge template as the ev charge team keeps it. */
data class EvChargeTemplate(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var weight: Double,
    var lane: String,
    var shiftCode: String,
    var etaMinutes: Int,
    var stopCount: Int,
    var quantity: Int,
    var timezone: String,
    var visible: Boolean,
    var status: EvChargeTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeTemplateStatus): Boolean {
        when (status) {
            EvChargeTemplateStatus.CREATED -> {
                when (next) {
                    EvChargeTemplateStatus.PUBLISHED -> return true
                }
                return false
            }
            EvChargeTemplateStatus.VALIDATED -> return next == EvChargeTemplateStatus.RETIRED
            EvChargeTemplateStatus.PUBLISHED -> {
                when (next) {
                    EvChargeTemplateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeTemplateStatus) {
        if (!canMoveTo(next)) {
            error("ev charge template $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"ev charge template"} $code"

    fun netTotal(): Double = round(quantity.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): EvChargeTemplate {
        return EvChargeTemplate(
            code = code,
            name = name,
            updatedAt = updatedAt,
            weight = weight,
            lane = lane,
            shiftCode = shiftCode,
            etaMinutes = etaMinutes,
            stopCount = stopCount,
            quantity = quantity,
            timezone = timezone,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a ev charge template in status created. */
fun newEvChargeTemplate(code: String, name: String): EvChargeTemplate {
    return EvChargeTemplate(
        code = code,
        name = name,
        updatedAt = 0,
        weight = 0.0,
        lane = "",
        shiftCode = "",
        etaMinutes = 0,
        stopCount = 0,
        quantity = 0,
        timezone = "",
        visible = false,
        status = EvChargeTemplateStatus.CREATED,
    )
}
