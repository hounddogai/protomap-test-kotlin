package com.acme.transport.crewing.restbreak.preference

import com.acme.transport.kit.text.normalize

// The rest break preference records of crewing's rest break work, and the rules, stores, and calculations
// around them.

/** Where a rest break preference is in its life cycle. */
enum class RestBreakPreferenceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a rest break preference and its current status. */
data class RestBreakPreference(
    var code: String,
    var name: String,
    var source: String,
    var expiresAt: Int,
    var label: String,
    var status: RestBreakPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakPreferenceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakPreferenceStatus): Boolean {
        when (status) {
            RestBreakPreferenceStatus.CREATED -> {
                when (next) {
                    RestBreakPreferenceStatus.PUBLISHED -> return true
                }
                return false
            }
            RestBreakPreferenceStatus.VALIDATED -> return next == RestBreakPreferenceStatus.RETIRED
            RestBreakPreferenceStatus.PUBLISHED -> return next == RestBreakPreferenceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RestBreakPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("rest break preference $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $name"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): RestBreakPreference {
        return RestBreakPreference(
            code = code,
            name = name,
            source = source,
            expiresAt = expiresAt,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a rest break preference in status created. */
fun newRestBreakPreference(code: String, name: String): RestBreakPreference {
    return RestBreakPreference(
        code = code,
        name = name,
        source = "",
        expiresAt = 0,
        label = "",
        status = RestBreakPreferenceStatus.CREATED,
    )
}
