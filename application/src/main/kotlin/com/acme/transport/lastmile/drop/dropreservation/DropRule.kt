package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop rule is in its life cycle. */
enum class DropRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A drop rule. Its code is unique within a tenant. */
data class DropRule(
    var code: String,
    var title: String,
    var phone: String,
    var priority: Int,
    var description: String,
    var name: String,
    var expiresAt: Int,
    var archived: Boolean,
    var status: DropRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropRuleStatus): Boolean {
        when (status) {
            DropRuleStatus.REQUESTED -> return next == DropRuleStatus.GRANTED
            DropRuleStatus.GRANTED -> return next == DropRuleStatus.EXPIRED
            DropRuleStatus.REVOKED -> return next == DropRuleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DropRuleStatus) {
        if (!canMoveTo(next)) {
            error("drop rule $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"drop rule"} $code"

    fun effectiveLoad(): Double = round(priority.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): DropRule {
        return DropRule(
            code = code,
            title = title,
            phone = phone,
            priority = priority,
            description = description,
            name = name,
            expiresAt = expiresAt,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a drop rule in status requested. */
fun newDropRule(code: String, title: String): DropRule {
    return DropRule(
        code = code,
        title = title,
        phone = "",
        priority = 0,
        description = "",
        name = "",
        expiresAt = 0,
        archived = false,
        status = DropRuleStatus.REQUESTED,
    )
}
