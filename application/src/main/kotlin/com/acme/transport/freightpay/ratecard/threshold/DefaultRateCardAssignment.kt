package com.acme.transport.freightpay.ratecard.threshold

import com.acme.transport.kit.text.normalize

/** Where a default rate card assignment is in its life cycle. */
enum class DefaultRateCardAssignmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A default rate card assignment as the rate card team keeps it. */
data class DefaultRateCardAssignment(
    var code: String,
    var name: String,
    var source: String,
    var contactEmail: String,
    var category: String,
    var attributes: MutableMap<String, String>,
    var region: String,
    var quantity: Int,
    var status: DefaultRateCardAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRateCardAssignmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRateCardAssignmentStatus): Boolean {
        when (status) {
            DefaultRateCardAssignmentStatus.NEW -> {
                when (next) {
                    DefaultRateCardAssignmentStatus.QUEUED, DefaultRateCardAssignmentStatus.RUNNING -> return true
                }
                return false
            }
            DefaultRateCardAssignmentStatus.QUEUED -> return next == DefaultRateCardAssignmentStatus.DONE || next == DefaultRateCardAssignmentStatus.FAILED
            DefaultRateCardAssignmentStatus.RUNNING -> {
                when (next) {
                    DefaultRateCardAssignmentStatus.DONE, DefaultRateCardAssignmentStatus.FAILED -> return true
                }
                return false
            }
            DefaultRateCardAssignmentStatus.DONE -> {
                when (next) {
                    DefaultRateCardAssignmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultRateCardAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("default rate card assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): DefaultRateCardAssignment {
        return DefaultRateCardAssignment(
            code = code,
            name = name,
            source = source,
            contactEmail = contactEmail,
            category = category,
            attributes = attributes,
            region = region,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a default rate card assignment in status new. */
fun newDefaultRateCardAssignment(code: String, name: String): DefaultRateCardAssignment {
    return DefaultRateCardAssignment(
        code = code,
        name = name,
        source = "",
        contactEmail = "",
        category = "",
        attributes = mutableMapOf<String, String>(),
        region = "",
        quantity = 0,
        status = DefaultRateCardAssignmentStatus.NEW,
    )
}
