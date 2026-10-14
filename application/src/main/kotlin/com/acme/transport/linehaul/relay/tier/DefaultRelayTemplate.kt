package com.acme.transport.linehaul.relay.tier

import com.acme.transport.kit.text.normalize

/** Where a default relay template is in its life cycle. */
enum class DefaultRelayTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One default relay template, with what the planners need to act on it. */
data class DefaultRelayTemplate(
    var code: String,
    var label: String,
    var ownerId: String,
    var rate: Double,
    var segment: String,
    var status: DefaultRelayTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRelayTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRelayTemplateStatus): Boolean {
        when (status) {
            DefaultRelayTemplateStatus.CREATED -> return next == DefaultRelayTemplateStatus.RETIRED
            DefaultRelayTemplateStatus.VALIDATED -> return next == DefaultRelayTemplateStatus.PUBLISHED || next == DefaultRelayTemplateStatus.RETIRED
            DefaultRelayTemplateStatus.PUBLISHED -> {
                when (next) {
                    DefaultRelayTemplateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultRelayTemplateStatus) {
        if (!canMoveTo(next)) {
            error("default relay template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = rate

    fun snapshot(): DefaultRelayTemplate {
        return DefaultRelayTemplate(
            code = code,
            label = label,
            ownerId = ownerId,
            rate = rate,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a default relay template in status created. */
fun newDefaultRelayTemplate(code: String, label: String): DefaultRelayTemplate {
    return DefaultRelayTemplate(
        code = code,
        label = label,
        ownerId = "",
        rate = 0.0,
        segment = "",
        status = DefaultRelayTemplateStatus.CREATED,
    )
}
