package com.acme.transport.emissions.idling.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling template is in its life cycle. */
enum class IdlingTemplateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A idling template as the idling team keeps it. */
data class IdlingTemplate(
    var code: String,
    var label: String,
    var quantity: Int,
    var durationMinutes: Int,
    var endsAt: Int,
    var status: IdlingTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingTemplateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingTemplateStatus): Boolean {
        when (status) {
            IdlingTemplateStatus.PROPOSED -> return next == IdlingTemplateStatus.APPROVED || next == IdlingTemplateStatus.WITHDRAWN
            IdlingTemplateStatus.APPROVED -> return next == IdlingTemplateStatus.REJECTED
            IdlingTemplateStatus.REJECTED -> return next == IdlingTemplateStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: IdlingTemplateStatus) {
        if (!canMoveTo(next)) {
            error("idling template $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveTotal(): Double = round(endsAt.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): IdlingTemplate {
        return IdlingTemplate(
            code = code,
            label = label,
            quantity = quantity,
            durationMinutes = durationMinutes,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a idling template in status proposed. */
fun newIdlingTemplate(code: String, label: String): IdlingTemplate {
    return IdlingTemplate(
        code = code,
        label = label,
        quantity = 0,
        durationMinutes = 0,
        endsAt = 0,
        status = IdlingTemplateStatus.PROPOSED,
    )
}
