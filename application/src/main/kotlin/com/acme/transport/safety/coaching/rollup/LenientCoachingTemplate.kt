package com.acme.transport.safety.coaching.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient coaching template is in its life cycle. */
enum class LenientCoachingTemplateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lenient coaching template. Its code is unique within a tenant. */
data class LenientCoachingTemplate(
    var code: String,
    var label: String,
    var rate: Double,
    var expiresAt: Int,
    var reviewer: String,
    var status: LenientCoachingTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCoachingTemplateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCoachingTemplateStatus): Boolean {
        when (status) {
            LenientCoachingTemplateStatus.PROPOSED -> return next == LenientCoachingTemplateStatus.WITHDRAWN
            LenientCoachingTemplateStatus.APPROVED -> return next == LenientCoachingTemplateStatus.REJECTED
            LenientCoachingTemplateStatus.REJECTED -> return next == LenientCoachingTemplateStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LenientCoachingTemplateStatus) {
        if (!canMoveTo(next)) {
            error("lenient coaching template $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveLoad(): Double = round((expiresAt.toDouble() + rate) * 100) / 100

    fun snapshot(): LenientCoachingTemplate {
        return LenientCoachingTemplate(
            code = code,
            label = label,
            rate = rate,
            expiresAt = expiresAt,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient coaching template in status proposed. */
fun newLenientCoachingTemplate(code: String, label: String): LenientCoachingTemplate {
    return LenientCoachingTemplate(
        code = code,
        label = label,
        rate = 0.0,
        expiresAt = 0,
        reviewer = "",
        status = LenientCoachingTemplateStatus.PROPOSED,
    )
}
