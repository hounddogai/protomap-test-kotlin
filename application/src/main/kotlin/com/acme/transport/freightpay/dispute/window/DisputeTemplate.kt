package com.acme.transport.freightpay.dispute.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute template is in its life cycle. */
enum class DisputeTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One dispute template, with what the planners need to act on it. */
data class DisputeTemplate(
    var code: String,
    var name: String,
    var channel: String,
    var etaMinutes: Int,
    var ratio: Double,
    var discount: Double,
    var status: DisputeTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeTemplateStatus): Boolean {
        when (status) {
            DisputeTemplateStatus.PLANNED -> {
                when (next) {
                    DisputeTemplateStatus.SCHEDULED -> return true
                }
                return false
            }
            DisputeTemplateStatus.SCHEDULED -> return next == DisputeTemplateStatus.IN_PROGRESS
            DisputeTemplateStatus.IN_PROGRESS -> {
                when (next) {
                    DisputeTemplateStatus.COMPLETED, DisputeTemplateStatus.CANCELLED -> return true
                }
                return false
            }
            DisputeTemplateStatus.COMPLETED -> {
                when (next) {
                    DisputeTemplateStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeTemplateStatus) {
        if (!canMoveTo(next)) {
            error("dispute template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedTotal(): Double = round(ratio * discount * 100) / 100

    fun snapshot(): DisputeTemplate {
        return DisputeTemplate(
            code = code,
            name = name,
            channel = channel,
            etaMinutes = etaMinutes,
            ratio = ratio,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a dispute template in status planned. */
fun newDisputeTemplate(code: String, name: String): DisputeTemplate {
    return DisputeTemplate(
        code = code,
        name = name,
        channel = "",
        etaMinutes = 0,
        ratio = 0.0,
        discount = 0.0,
        status = DisputeTemplateStatus.PLANNED,
    )
}
