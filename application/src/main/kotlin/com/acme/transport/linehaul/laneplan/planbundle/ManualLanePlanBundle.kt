package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.text.normalize

// The manual lane plan bundle records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a manual lane plan bundle is in its life cycle. */
enum class ManualLanePlanBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a manual lane plan bundle and its current status. */
data class ManualLanePlanBundle(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var depot: String,
    var summary: String,
    var ratio: Double,
    var ownerId: String,
    var status: ManualLanePlanBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualLanePlanBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualLanePlanBundleStatus): Boolean {
        when (status) {
            ManualLanePlanBundleStatus.REQUESTED -> {
                when (next) {
                    ManualLanePlanBundleStatus.GRANTED -> return true
                }
                return false
            }
            ManualLanePlanBundleStatus.GRANTED -> return next == ManualLanePlanBundleStatus.EXPIRED
            ManualLanePlanBundleStatus.REVOKED -> return next == ManualLanePlanBundleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ManualLanePlanBundleStatus) {
        if (!canMoveTo(next)) {
            error("manual lane plan bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual lane plan bundle"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = ratio

    fun snapshot(): ManualLanePlanBundle {
        return ManualLanePlanBundle(
            code = code,
            label = label,
            attributes = attributes,
            depot = depot,
            summary = summary,
            ratio = ratio,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a manual lane plan bundle in status requested. */
fun newManualLanePlanBundle(code: String, label: String): ManualLanePlanBundle {
    return ManualLanePlanBundle(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        depot = "",
        summary = "",
        ratio = 0.0,
        ownerId = "",
        status = ManualLanePlanBundleStatus.REQUESTED,
    )
}
