package com.acme.transport.emissions.idling.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary idling bundle records of emissions's idling work, and the rules, stores, and calculations
// around them.

/** Where a primary idling bundle is in its life cycle. */
enum class PrimaryIdlingBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A primary idling bundle as the idling team keeps it. */
data class PrimaryIdlingBundle(
    var code: String,
    var label: String,
    var category: String,
    var summary: String,
    var title: String,
    var timezone: String,
    var fuelLiters: Double,
    var startsAt: Int,
    var attributes: MutableMap<String, String>,
    var status: PrimaryIdlingBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryIdlingBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryIdlingBundleStatus): Boolean {
        when (status) {
            PrimaryIdlingBundleStatus.REQUESTED -> return next == PrimaryIdlingBundleStatus.GRANTED || next == PrimaryIdlingBundleStatus.REVOKED
            PrimaryIdlingBundleStatus.GRANTED -> {
                when (next) {
                    PrimaryIdlingBundleStatus.REVOKED, PrimaryIdlingBundleStatus.EXPIRED -> return true
                }
                return false
            }
            PrimaryIdlingBundleStatus.REVOKED -> {
                when (next) {
                    PrimaryIdlingBundleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryIdlingBundleStatus) {
        if (!canMoveTo(next)) {
            error("primary idling bundle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round((fuelLiters - startsAt.toDouble()) * 100) / 100

    fun snapshot(): PrimaryIdlingBundle {
        return PrimaryIdlingBundle(
            code = code,
            label = label,
            category = category,
            summary = summary,
            title = title,
            timezone = timezone,
            fuelLiters = fuelLiters,
            startsAt = startsAt,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a primary idling bundle in status requested. */
fun newPrimaryIdlingBundle(code: String, label: String): PrimaryIdlingBundle {
    return PrimaryIdlingBundle(
        code = code,
        label = label,
        category = "",
        summary = "",
        title = "",
        timezone = "",
        fuelLiters = 0.0,
        startsAt = 0,
        attributes = mutableMapOf<String, String>(),
        status = PrimaryIdlingBundleStatus.REQUESTED,
    )
}
