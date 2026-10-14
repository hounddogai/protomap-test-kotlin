package com.acme.transport.network.servicearea.areatarget

import com.acme.transport.kit.text.normalize

// The adaptive service area target records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a adaptive service area target is in its life cycle. */
enum class AdaptiveServiceAreaTargetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a adaptive service area target and its current status. */
data class AdaptiveServiceAreaTarget(
    var code: String,
    var label: String,
    var contactEmail: String,
    var summary: String,
    var locale: String,
    var dock: String,
    var version: Int,
    var routeCode: String,
    var description: String,
    var status: AdaptiveServiceAreaTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveServiceAreaTargetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveServiceAreaTargetStatus): Boolean {
        when (status) {
            AdaptiveServiceAreaTargetStatus.PROPOSED -> {
                when (next) {
                    AdaptiveServiceAreaTargetStatus.REJECTED -> return true
                }
                return false
            }
            AdaptiveServiceAreaTargetStatus.APPROVED -> {
                when (next) {
                    AdaptiveServiceAreaTargetStatus.REJECTED -> return true
                }
                return false
            }
            AdaptiveServiceAreaTargetStatus.REJECTED -> {
                when (next) {
                    AdaptiveServiceAreaTargetStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveServiceAreaTargetStatus) {
        if (!canMoveTo(next)) {
            error("adaptive service area target $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): AdaptiveServiceAreaTarget {
        return AdaptiveServiceAreaTarget(
            code = code,
            label = label,
            contactEmail = contactEmail,
            summary = summary,
            locale = locale,
            dock = dock,
            version = version,
            routeCode = routeCode,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a adaptive service area target in status proposed. */
fun newAdaptiveServiceAreaTarget(code: String, label: String): AdaptiveServiceAreaTarget {
    return AdaptiveServiceAreaTarget(
        code = code,
        label = label,
        contactEmail = "",
        summary = "",
        locale = "",
        dock = "",
        version = 0,
        routeCode = "",
        description = "",
        status = AdaptiveServiceAreaTargetStatus.PROPOSED,
    )
}
