package com.acme.transport.freightpay.ratecard.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive rate card template is in its life cycle. */
enum class AdaptiveRateCardTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One adaptive rate card template, with what the planners need to act on it. */
data class AdaptiveRateCardTemplate(
    var code: String,
    var title: String,
    var createdAt: Int,
    var capacity: Int,
    var durationMinutes: Int,
    var locale: String,
    var status: AdaptiveRateCardTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveRateCardTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveRateCardTemplateStatus): Boolean {
        when (status) {
            AdaptiveRateCardTemplateStatus.CREATED -> return next == AdaptiveRateCardTemplateStatus.RETIRED
            AdaptiveRateCardTemplateStatus.VALIDATED -> {
                when (next) {
                    AdaptiveRateCardTemplateStatus.PUBLISHED, AdaptiveRateCardTemplateStatus.RETIRED -> return true
                }
                return false
            }
            AdaptiveRateCardTemplateStatus.PUBLISHED -> {
                when (next) {
                    AdaptiveRateCardTemplateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveRateCardTemplateStatus) {
        if (!canMoveTo(next)) {
            error("adaptive rate card template $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive rate card template"} $code"

    fun netLoad(): Double = round((durationMinutes.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveRateCardTemplate {
        return AdaptiveRateCardTemplate(
            code = code,
            title = title,
            createdAt = createdAt,
            capacity = capacity,
            durationMinutes = durationMinutes,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a adaptive rate card template in status created. */
fun newAdaptiveRateCardTemplate(code: String, title: String): AdaptiveRateCardTemplate {
    return AdaptiveRateCardTemplate(
        code = code,
        title = title,
        createdAt = 0,
        capacity = 0,
        durationMinutes = 0,
        locale = "",
        status = AdaptiveRateCardTemplateStatus.CREATED,
    )
}
