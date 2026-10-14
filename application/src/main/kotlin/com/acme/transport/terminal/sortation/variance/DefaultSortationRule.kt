package com.acme.transport.terminal.sortation.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default sortation rule is in its life cycle. */
enum class DefaultSortationRuleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a default sortation rule and its current status. */
data class DefaultSortationRule(
    var code: String,
    var name: String,
    var weight: Double,
    var approved: Boolean,
    var endsAt: Int,
    var shiftCode: String,
    var createdAt: Int,
    var budget: Double,
    var status: DefaultSortationRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultSortationRuleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultSortationRuleStatus): Boolean {
        when (status) {
            DefaultSortationRuleStatus.CREATED -> return next == DefaultSortationRuleStatus.PUBLISHED || next == DefaultSortationRuleStatus.RETIRED
            DefaultSortationRuleStatus.VALIDATED -> return next == DefaultSortationRuleStatus.RETIRED
            DefaultSortationRuleStatus.PUBLISHED -> return next == DefaultSortationRuleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultSortationRuleStatus) {
        if (!canMoveTo(next)) {
            error("default sortation rule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default sortation rule"} $code"

    fun effectiveVolume(): Double = round(createdAt.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): DefaultSortationRule {
        return DefaultSortationRule(
            code = code,
            name = name,
            weight = weight,
            approved = approved,
            endsAt = endsAt,
            shiftCode = shiftCode,
            createdAt = createdAt,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default sortation rule in status created. */
fun newDefaultSortationRule(code: String, name: String): DefaultSortationRule {
    return DefaultSortationRule(
        code = code,
        name = name,
        weight = 0.0,
        approved = false,
        endsAt = 0,
        shiftCode = "",
        createdAt = 0,
        budget = 0.0,
        status = DefaultSortationRuleStatus.CREATED,
    )
}
