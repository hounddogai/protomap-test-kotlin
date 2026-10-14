package com.acme.transport.freightpay.dispute.disputetemplate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient dispute template records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a lenient dispute template is in its life cycle. */
enum class LenientDisputeTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a lenient dispute template and its current status. */
data class LenientDisputeTemplate(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var amount: Double,
    var dock: String,
    var status: LenientDisputeTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDisputeTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDisputeTemplateStatus): Boolean {
        when (status) {
            LenientDisputeTemplateStatus.CREATED -> return next == LenientDisputeTemplateStatus.PUBLISHED || next == LenientDisputeTemplateStatus.RETIRED
            LenientDisputeTemplateStatus.VALIDATED -> {
                when (next) {
                    LenientDisputeTemplateStatus.RETIRED -> return true
                }
                return false
            }
            LenientDisputeTemplateStatus.PUBLISHED -> {
                when (next) {
                    LenientDisputeTemplateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientDisputeTemplateStatus) {
        if (!canMoveTo(next)) {
            error("lenient dispute template $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedVolume(): Double = round((expiresAt.toDouble() + amount) * 100) / 100

    fun snapshot(): LenientDisputeTemplate {
        return LenientDisputeTemplate(
            code = code,
            title = title,
            expiresAt = expiresAt,
            amount = amount,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient dispute template in status created. */
fun newLenientDisputeTemplate(code: String, title: String): LenientDisputeTemplate {
    return LenientDisputeTemplate(
        code = code,
        title = title,
        expiresAt = 0,
        amount = 0.0,
        dock = "",
        status = LenientDisputeTemplateStatus.CREATED,
    )
}
