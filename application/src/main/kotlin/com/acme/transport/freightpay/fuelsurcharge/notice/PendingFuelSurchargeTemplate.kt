package com.acme.transport.freightpay.fuelsurcharge.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending fuel surcharge template is in its life cycle. */
enum class PendingFuelSurchargeTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a pending fuel surcharge template and its current status. */
data class PendingFuelSurchargeTemplate(
    var code: String,
    var name: String,
    var startsAt: Int,
    var locale: String,
    var pinned: Boolean,
    var ratio: Double,
    var minimum: Double,
    var visible: Boolean,
    var version: Int,
    var revision: Int,
    var status: PendingFuelSurchargeTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingFuelSurchargeTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingFuelSurchargeTemplateStatus): Boolean {
        when (status) {
            PendingFuelSurchargeTemplateStatus.CREATED -> {
                when (next) {
                    PendingFuelSurchargeTemplateStatus.VALIDATED, PendingFuelSurchargeTemplateStatus.RETIRED -> return true
                }
                return false
            }
            PendingFuelSurchargeTemplateStatus.VALIDATED -> {
                when (next) {
                    PendingFuelSurchargeTemplateStatus.PUBLISHED, PendingFuelSurchargeTemplateStatus.RETIRED -> return true
                }
                return false
            }
            PendingFuelSurchargeTemplateStatus.PUBLISHED -> return next == PendingFuelSurchargeTemplateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PendingFuelSurchargeTemplateStatus) {
        if (!canMoveTo(next)) {
            error("pending fuel surcharge template $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"pending fuel surcharge template"} $code"

    fun projectedValue(): Double = round((version.toDouble() - ratio) * 100) / 100

    fun snapshot(): PendingFuelSurchargeTemplate {
        return PendingFuelSurchargeTemplate(
            code = code,
            name = name,
            startsAt = startsAt,
            locale = locale,
            pinned = pinned,
            ratio = ratio,
            minimum = minimum,
            visible = visible,
            version = version,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a pending fuel surcharge template in status created. */
fun newPendingFuelSurchargeTemplate(code: String, name: String): PendingFuelSurchargeTemplate {
    return PendingFuelSurchargeTemplate(
        code = code,
        name = name,
        startsAt = 0,
        locale = "",
        pinned = false,
        ratio = 0.0,
        minimum = 0.0,
        visible = false,
        version = 0,
        revision = 0,
        status = PendingFuelSurchargeTemplateStatus.CREATED,
    )
}
