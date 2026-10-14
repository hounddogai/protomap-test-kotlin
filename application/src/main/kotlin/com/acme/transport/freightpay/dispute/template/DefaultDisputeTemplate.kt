package com.acme.transport.freightpay.dispute.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default dispute template records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a default dispute template is in its life cycle. */
enum class DefaultDisputeTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A default dispute template as the dispute team keeps it. */
data class DefaultDisputeTemplate(
    var code: String,
    var title: String,
    var externalRef: String,
    var ownerId: String,
    var sequence: Int,
    var ceiling: Double,
    var status: DefaultDisputeTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDisputeTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDisputeTemplateStatus): Boolean {
        when (status) {
            DefaultDisputeTemplateStatus.DRAFT -> {
                when (next) {
                    DefaultDisputeTemplateStatus.CLOSED -> return true
                }
                return false
            }
            DefaultDisputeTemplateStatus.PENDING -> {
                when (next) {
                    DefaultDisputeTemplateStatus.ACTIVE, DefaultDisputeTemplateStatus.SUSPENDED -> return true
                }
                return false
            }
            DefaultDisputeTemplateStatus.ACTIVE -> {
                when (next) {
                    DefaultDisputeTemplateStatus.SUSPENDED, DefaultDisputeTemplateStatus.CLOSED -> return true
                }
                return false
            }
            DefaultDisputeTemplateStatus.SUSPENDED -> {
                when (next) {
                    DefaultDisputeTemplateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultDisputeTemplateStatus) {
        if (!canMoveTo(next)) {
            error("default dispute template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedLoad(): Double = round(ceiling * sequence.toDouble() * 100) / 100

    fun snapshot(): DefaultDisputeTemplate {
        return DefaultDisputeTemplate(
            code = code,
            title = title,
            externalRef = externalRef,
            ownerId = ownerId,
            sequence = sequence,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default dispute template in status draft. */
fun newDefaultDisputeTemplate(code: String, title: String): DefaultDisputeTemplate {
    return DefaultDisputeTemplate(
        code = code,
        title = title,
        externalRef = "",
        ownerId = "",
        sequence = 0,
        ceiling = 0.0,
        status = DefaultDisputeTemplateStatus.DRAFT,
    )
}
