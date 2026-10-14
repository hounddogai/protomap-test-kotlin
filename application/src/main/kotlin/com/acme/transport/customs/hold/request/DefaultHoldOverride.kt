package com.acme.transport.customs.hold.request

import com.acme.transport.kit.text.normalize

/** Where a default hold override is in its life cycle. */
enum class DefaultHoldOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A default hold override as the hold team keeps it. */
data class DefaultHoldOverride(
    var code: String,
    var label: String,
    var quantity: Int,
    var region: String,
    var name: String,
    var description: String,
    var status: DefaultHoldOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultHoldOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultHoldOverrideStatus): Boolean {
        when (status) {
            DefaultHoldOverrideStatus.OPEN -> {
                when (next) {
                    DefaultHoldOverrideStatus.IN_REVIEW, DefaultHoldOverrideStatus.RESOLVED -> return true
                }
                return false
            }
            DefaultHoldOverrideStatus.IN_REVIEW -> return next == DefaultHoldOverrideStatus.REOPENED || next == DefaultHoldOverrideStatus.ARCHIVED
            DefaultHoldOverrideStatus.RESOLVED -> return next == DefaultHoldOverrideStatus.REOPENED || next == DefaultHoldOverrideStatus.ARCHIVED
            DefaultHoldOverrideStatus.REOPENED -> return next == DefaultHoldOverrideStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DefaultHoldOverrideStatus) {
        if (!canMoveTo(next)) {
            error("default hold override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"default hold override"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): DefaultHoldOverride {
        return DefaultHoldOverride(
            code = code,
            label = label,
            quantity = quantity,
            region = region,
            name = name,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a default hold override in status open. */
fun newDefaultHoldOverride(code: String, label: String): DefaultHoldOverride {
    return DefaultHoldOverride(
        code = code,
        label = label,
        quantity = 0,
        region = "",
        name = "",
        description = "",
        status = DefaultHoldOverrideStatus.OPEN,
    )
}
