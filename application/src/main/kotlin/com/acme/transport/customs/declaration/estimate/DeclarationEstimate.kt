package com.acme.transport.customs.declaration.estimate

import com.acme.transport.kit.text.normalize

// The declaration estimate records of customs's declaration work, and the rules, stores, and calculations
// around them.

/** Where a declaration estimate is in its life cycle. */
enum class DeclarationEstimateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One declaration estimate, with what the planners need to act on it. */
data class DeclarationEstimate(
    var code: String,
    var title: String,
    var windowDays: Int,
    var attributes: MutableMap<String, String>,
    var visible: Boolean,
    var status: DeclarationEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationEstimateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationEstimateStatus): Boolean {
        when (status) {
            DeclarationEstimateStatus.DRAFT -> return next == DeclarationEstimateStatus.ACTIVE || next == DeclarationEstimateStatus.SUSPENDED
            DeclarationEstimateStatus.PENDING -> {
                when (next) {
                    DeclarationEstimateStatus.ACTIVE -> return true
                }
                return false
            }
            DeclarationEstimateStatus.ACTIVE -> {
                when (next) {
                    DeclarationEstimateStatus.CLOSED -> return true
                }
                return false
            }
            DeclarationEstimateStatus.SUSPENDED -> return next == DeclarationEstimateStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DeclarationEstimateStatus) {
        if (!canMoveTo(next)) {
            error("declaration estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): DeclarationEstimate {
        return DeclarationEstimate(
            code = code,
            title = title,
            windowDays = windowDays,
            attributes = attributes,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a declaration estimate in status draft. */
fun newDeclarationEstimate(code: String, title: String): DeclarationEstimate {
    return DeclarationEstimate(
        code = code,
        title = title,
        windowDays = 0,
        attributes = mutableMapOf<String, String>(),
        visible = false,
        status = DeclarationEstimateStatus.DRAFT,
    )
}
