package com.acme.transport.linehaul.relay.summary

import com.acme.transport.kit.text.normalize

// The relay summary records of linehaul's relay work, and the rules, stores, and calculations around them.

/** Where a relay summary is in its life cycle. */
enum class RelaySummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a relay summary and its current status. */
data class RelaySummary(
    var code: String,
    var name: String,
    var dock: String,
    var attributes: MutableMap<String, String>,
    var channel: String,
    var shiftCode: String,
    var externalRef: String,
    var quantity: Int,
    var status: RelaySummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelaySummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelaySummaryStatus): Boolean {
        when (status) {
            RelaySummaryStatus.PLANNED -> return next == RelaySummaryStatus.CANCELLED
            RelaySummaryStatus.SCHEDULED -> {
                when (next) {
                    RelaySummaryStatus.CANCELLED -> return true
                }
                return false
            }
            RelaySummaryStatus.IN_PROGRESS -> {
                when (next) {
                    RelaySummaryStatus.CANCELLED -> return true
                }
                return false
            }
            RelaySummaryStatus.COMPLETED -> {
                when (next) {
                    RelaySummaryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelaySummaryStatus) {
        if (!canMoveTo(next)) {
            error("relay summary $code cannot move from $status to $next")
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

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): RelaySummary {
        return RelaySummary(
            code = code,
            name = name,
            dock = dock,
            attributes = attributes,
            channel = channel,
            shiftCode = shiftCode,
            externalRef = externalRef,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a relay summary in status planned. */
fun newRelaySummary(code: String, name: String): RelaySummary {
    return RelaySummary(
        code = code,
        name = name,
        dock = "",
        attributes = mutableMapOf<String, String>(),
        channel = "",
        shiftCode = "",
        externalRef = "",
        quantity = 0,
        status = RelaySummaryStatus.PLANNED,
    )
}
