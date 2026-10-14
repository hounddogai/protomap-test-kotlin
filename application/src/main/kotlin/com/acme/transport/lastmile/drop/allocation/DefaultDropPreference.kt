package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default drop preference is in its life cycle. */
enum class DefaultDropPreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A default drop preference as the drop team keeps it. */
data class DefaultDropPreference(
    var code: String,
    var title: String,
    var quantity: Int,
    var name: String,
    var floorValue: Double,
    var stopCount: Int,
    var summary: String,
    var carrier: String,
    var status: DefaultDropPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDropPreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDropPreferenceStatus): Boolean {
        when (status) {
            DefaultDropPreferenceStatus.PROPOSED -> {
                when (next) {
                    DefaultDropPreferenceStatus.APPROVED, DefaultDropPreferenceStatus.REJECTED -> return true
                }
                return false
            }
            DefaultDropPreferenceStatus.APPROVED -> {
                when (next) {
                    DefaultDropPreferenceStatus.REJECTED, DefaultDropPreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefaultDropPreferenceStatus.REJECTED -> return next == DefaultDropPreferenceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DefaultDropPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("default drop preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netExposure(): Double = round((quantity.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): DefaultDropPreference {
        return DefaultDropPreference(
            code = code,
            title = title,
            quantity = quantity,
            name = name,
            floorValue = floorValue,
            stopCount = stopCount,
            summary = summary,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default drop preference in status proposed. */
fun newDefaultDropPreference(code: String, title: String): DefaultDropPreference {
    return DefaultDropPreference(
        code = code,
        title = title,
        quantity = 0,
        name = "",
        floorValue = 0.0,
        stopCount = 0,
        summary = "",
        carrier = "",
        status = DefaultDropPreferenceStatus.PROPOSED,
    )
}
