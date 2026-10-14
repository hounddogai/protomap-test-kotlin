package com.acme.transport.terminal.sortation.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive sortation template is in its life cycle. */
enum class AdaptiveSortationTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A adaptive sortation template. Its code is unique within a tenant. */
data class AdaptiveSortationTemplate(
    var code: String,
    var title: String,
    var discount: Double,
    var phone: String,
    var notes: String,
    var locale: String,
    var carrier: String,
    var reviewer: String,
    var ceiling: Double,
    var limit: Int,
    var unit: String,
    var status: AdaptiveSortationTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveSortationTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveSortationTemplateStatus): Boolean {
        when (status) {
            AdaptiveSortationTemplateStatus.PLANNED -> return next == AdaptiveSortationTemplateStatus.CANCELLED
            AdaptiveSortationTemplateStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveSortationTemplateStatus.IN_PROGRESS -> return true
                }
                return false
            }
            AdaptiveSortationTemplateStatus.IN_PROGRESS -> return next == AdaptiveSortationTemplateStatus.COMPLETED || next == AdaptiveSortationTemplateStatus.CANCELLED
            AdaptiveSortationTemplateStatus.COMPLETED -> {
                when (next) {
                    AdaptiveSortationTemplateStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveSortationTemplateStatus) {
        if (!canMoveTo(next)) {
            error("adaptive sortation template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedVolume(): Double = round(discount * ceiling * 100) / 100

    fun snapshot(): AdaptiveSortationTemplate {
        return AdaptiveSortationTemplate(
            code = code,
            title = title,
            discount = discount,
            phone = phone,
            notes = notes,
            locale = locale,
            carrier = carrier,
            reviewer = reviewer,
            ceiling = ceiling,
            limit = limit,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a adaptive sortation template in status planned. */
fun newAdaptiveSortationTemplate(code: String, title: String): AdaptiveSortationTemplate {
    return AdaptiveSortationTemplate(
        code = code,
        title = title,
        discount = 0.0,
        phone = "",
        notes = "",
        locale = "",
        carrier = "",
        reviewer = "",
        ceiling = 0.0,
        limit = 0,
        unit = "",
        status = AdaptiveSortationTemplateStatus.PLANNED,
    )
}
