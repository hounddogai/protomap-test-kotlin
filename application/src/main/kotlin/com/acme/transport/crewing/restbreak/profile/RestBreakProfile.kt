package com.acme.transport.crewing.restbreak.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rest break profile records of crewing's rest break work, and the rules, stores, and calculations around
// them.

/** Where a rest break profile is in its life cycle. */
enum class RestBreakProfileStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One rest break profile, with what the planners need to act on it. */
data class RestBreakProfile(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var routeCode: String,
    var quantity: Int,
    var phone: String,
    var stopCount: Int,
    var status: RestBreakProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakProfileStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakProfileStatus): Boolean {
        when (status) {
            RestBreakProfileStatus.OPEN -> return next == RestBreakProfileStatus.REOPENED
            RestBreakProfileStatus.IN_REVIEW -> return next == RestBreakProfileStatus.REOPENED || next == RestBreakProfileStatus.ARCHIVED
            RestBreakProfileStatus.RESOLVED -> return next == RestBreakProfileStatus.REOPENED
            RestBreakProfileStatus.REOPENED -> return next == RestBreakProfileStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RestBreakProfileStatus) {
        if (!canMoveTo(next)) {
            error("rest break profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round((quantity.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): RestBreakProfile {
        return RestBreakProfile(
            code = code,
            name = name,
            attributes = attributes,
            routeCode = routeCode,
            quantity = quantity,
            phone = phone,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rest break profile in status open. */
fun newRestBreakProfile(code: String, name: String): RestBreakProfile {
    return RestBreakProfile(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        routeCode = "",
        quantity = 0,
        phone = "",
        stopCount = 0,
        status = RestBreakProfileStatus.OPEN,
    )
}
