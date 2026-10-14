package com.acme.transport.crewing.roster.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default roster rule is in its life cycle. */
enum class DefaultRosterRuleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A default roster rule. Its code is unique within a tenant. */
data class DefaultRosterRule(
    var code: String,
    var title: String,
    var reviewer: String,
    var quantity: Int,
    var name: String,
    var margin: Double,
    var endsAt: Int,
    var checksum: String,
    var contactEmail: String,
    var budget: Double,
    var description: String,
    var status: DefaultRosterRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRosterRuleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRosterRuleStatus): Boolean {
        when (status) {
            DefaultRosterRuleStatus.DRAFT -> return next == DefaultRosterRuleStatus.PENDING || next == DefaultRosterRuleStatus.SUSPENDED
            DefaultRosterRuleStatus.PENDING -> {
                when (next) {
                    DefaultRosterRuleStatus.SUSPENDED -> return true
                }
                return false
            }
            DefaultRosterRuleStatus.ACTIVE -> {
                when (next) {
                    DefaultRosterRuleStatus.SUSPENDED, DefaultRosterRuleStatus.CLOSED -> return true
                }
                return false
            }
            DefaultRosterRuleStatus.SUSPENDED -> {
                when (next) {
                    DefaultRosterRuleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultRosterRuleStatus) {
        if (!canMoveTo(next)) {
            error("default roster rule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netValue(): Double = round((budget - quantity.toDouble()) * 100) / 100

    fun snapshot(): DefaultRosterRule {
        return DefaultRosterRule(
            code = code,
            title = title,
            reviewer = reviewer,
            quantity = quantity,
            name = name,
            margin = margin,
            endsAt = endsAt,
            checksum = checksum,
            contactEmail = contactEmail,
            budget = budget,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default roster rule in status draft. */
fun newDefaultRosterRule(code: String, title: String): DefaultRosterRule {
    return DefaultRosterRule(
        code = code,
        title = title,
        reviewer = "",
        quantity = 0,
        name = "",
        margin = 0.0,
        endsAt = 0,
        checksum = "",
        contactEmail = "",
        budget = 0.0,
        description = "",
        status = DefaultRosterRuleStatus.DRAFT,
    )
}
