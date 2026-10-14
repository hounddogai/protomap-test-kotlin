package com.acme.transport.emissions.idling.review

import com.acme.transport.kit.text.normalize

/** Where a manual idling rule is in its life cycle. */
enum class ManualIdlingRuleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A manual idling rule. Its code is unique within a tenant. */
data class ManualIdlingRule(
    var code: String,
    var title: String,
    var trailerId: String,
    var contactEmail: String,
    var rate: Double,
    var pinned: Boolean,
    var name: String,
    var status: ManualIdlingRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualIdlingRuleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualIdlingRuleStatus): Boolean {
        when (status) {
            ManualIdlingRuleStatus.DRAFT -> return next == ManualIdlingRuleStatus.CLOSED
            ManualIdlingRuleStatus.PENDING -> return next == ManualIdlingRuleStatus.SUSPENDED || next == ManualIdlingRuleStatus.CLOSED
            ManualIdlingRuleStatus.ACTIVE -> {
                when (next) {
                    ManualIdlingRuleStatus.SUSPENDED, ManualIdlingRuleStatus.CLOSED -> return true
                }
                return false
            }
            ManualIdlingRuleStatus.SUSPENDED -> {
                when (next) {
                    ManualIdlingRuleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualIdlingRuleStatus) {
        if (!canMoveTo(next)) {
            error("manual idling rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = rate

    fun snapshot(): ManualIdlingRule {
        return ManualIdlingRule(
            code = code,
            title = title,
            trailerId = trailerId,
            contactEmail = contactEmail,
            rate = rate,
            pinned = pinned,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual idling rule in status draft. */
fun newManualIdlingRule(code: String, title: String): ManualIdlingRule {
    return ManualIdlingRule(
        code = code,
        title = title,
        trailerId = "",
        contactEmail = "",
        rate = 0.0,
        pinned = false,
        name = "",
        status = ManualIdlingRuleStatus.DRAFT,
    )
}
