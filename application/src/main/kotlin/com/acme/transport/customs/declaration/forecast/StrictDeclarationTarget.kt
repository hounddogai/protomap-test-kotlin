package com.acme.transport.customs.declaration.forecast

import com.acme.transport.kit.text.normalize

/** Where a strict declaration target is in its life cycle. */
enum class StrictDeclarationTargetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A strict declaration target as the declaration team keeps it. */
data class StrictDeclarationTarget(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var approved: Boolean,
    var attributes: MutableMap<String, String>,
    var weight: Double,
    var country: String,
    var name: String,
    var status: StrictDeclarationTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDeclarationTargetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDeclarationTargetStatus): Boolean {
        when (status) {
            StrictDeclarationTargetStatus.REQUESTED -> {
                when (next) {
                    StrictDeclarationTargetStatus.REVOKED, StrictDeclarationTargetStatus.EXPIRED -> return true
                }
                return false
            }
            StrictDeclarationTargetStatus.GRANTED -> {
                when (next) {
                    StrictDeclarationTargetStatus.EXPIRED -> return true
                }
                return false
            }
            StrictDeclarationTargetStatus.REVOKED -> return next == StrictDeclarationTargetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: StrictDeclarationTargetStatus) {
        if (!canMoveTo(next)) {
            error("strict declaration target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"strict declaration target"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = weight

    fun snapshot(): StrictDeclarationTarget {
        return StrictDeclarationTarget(
            code = code,
            label = label,
            tags = tags,
            approved = approved,
            attributes = attributes,
            weight = weight,
            country = country,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a strict declaration target in status requested. */
fun newStrictDeclarationTarget(code: String, label: String): StrictDeclarationTarget {
    return StrictDeclarationTarget(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        approved = false,
        attributes = mutableMapOf<String, String>(),
        weight = 0.0,
        country = "",
        name = "",
        status = StrictDeclarationTargetStatus.REQUESTED,
    )
}
