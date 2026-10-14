package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered backhaul preference is in its life cycle. */
enum class TieredBackhaulPreferenceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered backhaul preference. Its code is unique within a tenant. */
data class TieredBackhaulPreference(
    var code: String,
    var label: String,
    var routeCode: String,
    var pinned: Boolean,
    var discount: Double,
    var limit: Int,
    var externalRef: String,
    var threshold: Double,
    var sequence: Int,
    var maximum: Double,
    var channel: String,
    var status: TieredBackhaulPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredBackhaulPreferenceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredBackhaulPreferenceStatus): Boolean {
        when (status) {
            TieredBackhaulPreferenceStatus.OPEN -> {
                when (next) {
                    TieredBackhaulPreferenceStatus.IN_REVIEW, TieredBackhaulPreferenceStatus.RESOLVED -> return true
                }
                return false
            }
            TieredBackhaulPreferenceStatus.IN_REVIEW -> return next == TieredBackhaulPreferenceStatus.REOPENED || next == TieredBackhaulPreferenceStatus.ARCHIVED
            TieredBackhaulPreferenceStatus.RESOLVED -> return next == TieredBackhaulPreferenceStatus.REOPENED
            TieredBackhaulPreferenceStatus.REOPENED -> return next == TieredBackhaulPreferenceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredBackhaulPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("tiered backhaul preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveValue(): Double = round((sequence.toDouble() + maximum) * 100) / 100

    fun snapshot(): TieredBackhaulPreference {
        return TieredBackhaulPreference(
            code = code,
            label = label,
            routeCode = routeCode,
            pinned = pinned,
            discount = discount,
            limit = limit,
            externalRef = externalRef,
            threshold = threshold,
            sequence = sequence,
            maximum = maximum,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a tiered backhaul preference in status open. */
fun newTieredBackhaulPreference(code: String, label: String): TieredBackhaulPreference {
    return TieredBackhaulPreference(
        code = code,
        label = label,
        routeCode = "",
        pinned = false,
        discount = 0.0,
        limit = 0,
        externalRef = "",
        threshold = 0.0,
        sequence = 0,
        maximum = 0.0,
        channel = "",
        status = TieredBackhaulPreferenceStatus.OPEN,
    )
}
