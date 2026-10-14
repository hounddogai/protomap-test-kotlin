package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.text.normalize

/** Where a default relay baseline is in its life cycle. */
enum class DefaultRelayBaselineStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a default relay baseline and its current status. */
data class DefaultRelayBaseline(
    var code: String,
    var label: String,
    var discount: Double,
    var category: String,
    var dock: String,
    var segment: String,
    var lane: String,
    var description: String,
    var reviewer: String,
    var pinned: Boolean,
    var status: DefaultRelayBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRelayBaselineStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRelayBaselineStatus): Boolean {
        when (status) {
            DefaultRelayBaselineStatus.CREATED -> {
                when (next) {
                    DefaultRelayBaselineStatus.VALIDATED, DefaultRelayBaselineStatus.PUBLISHED -> return true
                }
                return false
            }
            DefaultRelayBaselineStatus.VALIDATED -> return next == DefaultRelayBaselineStatus.PUBLISHED || next == DefaultRelayBaselineStatus.RETIRED
            DefaultRelayBaselineStatus.PUBLISHED -> return next == DefaultRelayBaselineStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultRelayBaselineStatus) {
        if (!canMoveTo(next)) {
            error("default relay baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"default relay baseline"} $code"

    fun measuredValue(): Double = discount

    fun snapshot(): DefaultRelayBaseline {
        return DefaultRelayBaseline(
            code = code,
            label = label,
            discount = discount,
            category = category,
            dock = dock,
            segment = segment,
            lane = lane,
            description = description,
            reviewer = reviewer,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a default relay baseline in status created. */
fun newDefaultRelayBaseline(code: String, label: String): DefaultRelayBaseline {
    return DefaultRelayBaseline(
        code = code,
        label = label,
        discount = 0.0,
        category = "",
        dock = "",
        segment = "",
        lane = "",
        description = "",
        reviewer = "",
        pinned = false,
        status = DefaultRelayBaselineStatus.CREATED,
    )
}
