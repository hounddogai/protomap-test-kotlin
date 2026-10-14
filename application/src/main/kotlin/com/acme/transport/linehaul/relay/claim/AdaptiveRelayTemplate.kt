package com.acme.transport.linehaul.relay.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive relay template is in its life cycle. */
enum class AdaptiveRelayTemplateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A adaptive relay template as the relay team keeps it. */
data class AdaptiveRelayTemplate(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var revision: Int,
    var amount: Double,
    var description: String,
    var tags: MutableList<String>,
    var status: AdaptiveRelayTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveRelayTemplateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveRelayTemplateStatus): Boolean {
        when (status) {
            AdaptiveRelayTemplateStatus.PROPOSED -> return next == AdaptiveRelayTemplateStatus.WITHDRAWN
            AdaptiveRelayTemplateStatus.APPROVED -> return next == AdaptiveRelayTemplateStatus.REJECTED || next == AdaptiveRelayTemplateStatus.WITHDRAWN
            AdaptiveRelayTemplateStatus.REJECTED -> return next == AdaptiveRelayTemplateStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AdaptiveRelayTemplateStatus) {
        if (!canMoveTo(next)) {
            error("adaptive relay template $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"adaptive relay template"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveValue(): Double = round(revision.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): AdaptiveRelayTemplate {
        return AdaptiveRelayTemplate(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            revision = revision,
            amount = amount,
            description = description,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive relay template in status proposed. */
fun newAdaptiveRelayTemplate(code: String, name: String): AdaptiveRelayTemplate {
    return AdaptiveRelayTemplate(
        code = code,
        name = name,
        durationMinutes = 0,
        revision = 0,
        amount = 0.0,
        description = "",
        tags = mutableListOf<String>(),
        status = AdaptiveRelayTemplateStatus.PROPOSED,
    )
}
