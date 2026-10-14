package com.acme.transport.customs.declaration.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The declaration audit records of customs's declaration work, and the rules, stores, and calculations around
// them.

/** Where a declaration audit is in its life cycle. */
enum class DeclarationAuditStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One declaration audit, with what the planners need to act on it. */
data class DeclarationAudit(
    var code: String,
    var title: String,
    var createdAt: Int,
    var carrier: String,
    var attributes: MutableMap<String, String>,
    var cost: Double,
    var approved: Boolean,
    var loadKg: Double,
    var status: DeclarationAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationAuditStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationAuditStatus): Boolean {
        when (status) {
            DeclarationAuditStatus.DRAFT -> {
                when (next) {
                    DeclarationAuditStatus.ACTIVE -> return true
                }
                return false
            }
            DeclarationAuditStatus.PENDING -> {
                when (next) {
                    DeclarationAuditStatus.ACTIVE, DeclarationAuditStatus.CLOSED -> return true
                }
                return false
            }
            DeclarationAuditStatus.ACTIVE -> return next == DeclarationAuditStatus.SUSPENDED || next == DeclarationAuditStatus.CLOSED
            DeclarationAuditStatus.SUSPENDED -> {
                when (next) {
                    DeclarationAuditStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationAuditStatus) {
        if (!canMoveTo(next)) {
            error("declaration audit $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round(cost * createdAt.toDouble() * 100) / 100

    fun snapshot(): DeclarationAudit {
        return DeclarationAudit(
            code = code,
            title = title,
            createdAt = createdAt,
            carrier = carrier,
            attributes = attributes,
            cost = cost,
            approved = approved,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a declaration audit in status draft. */
fun newDeclarationAudit(code: String, title: String): DeclarationAudit {
    return DeclarationAudit(
        code = code,
        title = title,
        createdAt = 0,
        carrier = "",
        attributes = mutableMapOf<String, String>(),
        cost = 0.0,
        approved = false,
        loadKg = 0.0,
        status = DeclarationAuditStatus.DRAFT,
    )
}
