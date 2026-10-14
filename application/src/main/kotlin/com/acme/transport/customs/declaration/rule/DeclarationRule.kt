package com.acme.transport.customs.declaration.rule

import com.acme.transport.kit.text.normalize

// The declaration rule records of customs's declaration work, and the rules, stores, and calculations around
// them.

/** Where a declaration rule is in its life cycle. */
enum class DeclarationRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a declaration rule and its current status. */
data class DeclarationRule(
    var code: String,
    var name: String,
    var attempts: Int,
    var externalRef: String,
    var attributes: MutableMap<String, String>,
    var notes: String,
    var status: DeclarationRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationRuleStatus): Boolean {
        when (status) {
            DeclarationRuleStatus.IDLE -> return next == DeclarationRuleStatus.WARMING || next == DeclarationRuleStatus.READY
            DeclarationRuleStatus.WARMING -> {
                when (next) {
                    DeclarationRuleStatus.READY, DeclarationRuleStatus.DRAINING -> return true
                }
                return false
            }
            DeclarationRuleStatus.READY -> return next == DeclarationRuleStatus.STOPPED
            DeclarationRuleStatus.DRAINING -> {
                when (next) {
                    DeclarationRuleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationRuleStatus) {
        if (!canMoveTo(next)) {
            error("declaration rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): DeclarationRule {
        return DeclarationRule(
            code = code,
            name = name,
            attempts = attempts,
            externalRef = externalRef,
            attributes = attributes,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a declaration rule in status idle. */
fun newDeclarationRule(code: String, name: String): DeclarationRule {
    return DeclarationRule(
        code = code,
        name = name,
        attempts = 0,
        externalRef = "",
        attributes = mutableMapOf<String, String>(),
        notes = "",
        status = DeclarationRuleStatus.IDLE,
    )
}
