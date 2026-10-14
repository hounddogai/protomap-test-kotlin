package com.acme.transport.network.cutoff.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient cutoff template records of network's cutoff work, and the rules, stores, and calculations
// around them.

/** Where a lenient cutoff template is in its life cycle. */
enum class LenientCutoffTemplateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lenient cutoff template as the cutoff team keeps it. */
data class LenientCutoffTemplate(
    var code: String,
    var title: String,
    var channel: String,
    var loadKg: Double,
    var quantity: Int,
    var status: LenientCutoffTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCutoffTemplateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCutoffTemplateStatus): Boolean {
        when (status) {
            LenientCutoffTemplateStatus.PROPOSED -> {
                when (next) {
                    LenientCutoffTemplateStatus.REJECTED -> return true
                }
                return false
            }
            LenientCutoffTemplateStatus.APPROVED -> {
                when (next) {
                    LenientCutoffTemplateStatus.REJECTED -> return true
                }
                return false
            }
            LenientCutoffTemplateStatus.REJECTED -> return next == LenientCutoffTemplateStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LenientCutoffTemplateStatus) {
        if (!canMoveTo(next)) {
            error("lenient cutoff template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedValue(): Double = round(loadKg * quantity.toDouble() * 100) / 100

    fun snapshot(): LenientCutoffTemplate {
        return LenientCutoffTemplate(
            code = code,
            title = title,
            channel = channel,
            loadKg = loadKg,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a lenient cutoff template in status proposed. */
fun newLenientCutoffTemplate(code: String, title: String): LenientCutoffTemplate {
    return LenientCutoffTemplate(
        code = code,
        title = title,
        channel = "",
        loadKg = 0.0,
        quantity = 0,
        status = LenientCutoffTemplateStatus.PROPOSED,
    )
}
