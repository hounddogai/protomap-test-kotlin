package com.acme.transport.customs.broker.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global broker override is in its life cycle. */
enum class GlobalBrokerOverrideStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a global broker override and its current status. */
data class GlobalBrokerOverride(
    var code: String,
    var title: String,
    var startsAt: Int,
    var source: String,
    var archived: Boolean,
    var locale: String,
    var margin: Double,
    var status: GlobalBrokerOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalBrokerOverrideStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalBrokerOverrideStatus): Boolean {
        when (status) {
            GlobalBrokerOverrideStatus.PROPOSED -> {
                when (next) {
                    GlobalBrokerOverrideStatus.REJECTED -> return true
                }
                return false
            }
            GlobalBrokerOverrideStatus.APPROVED -> return next == GlobalBrokerOverrideStatus.REJECTED || next == GlobalBrokerOverrideStatus.WITHDRAWN
            GlobalBrokerOverrideStatus.REJECTED -> {
                when (next) {
                    GlobalBrokerOverrideStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalBrokerOverrideStatus) {
        if (!canMoveTo(next)) {
            error("global broker override $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global broker override"} $code"

    fun netValue(): Double = round((startsAt.toDouble() + margin) * 100) / 100

    fun snapshot(): GlobalBrokerOverride {
        return GlobalBrokerOverride(
            code = code,
            title = title,
            startsAt = startsAt,
            source = source,
            archived = archived,
            locale = locale,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a global broker override in status proposed. */
fun newGlobalBrokerOverride(code: String, title: String): GlobalBrokerOverride {
    return GlobalBrokerOverride(
        code = code,
        title = title,
        startsAt = 0,
        source = "",
        archived = false,
        locale = "",
        margin = 0.0,
        status = GlobalBrokerOverrideStatus.PROPOSED,
    )
}
