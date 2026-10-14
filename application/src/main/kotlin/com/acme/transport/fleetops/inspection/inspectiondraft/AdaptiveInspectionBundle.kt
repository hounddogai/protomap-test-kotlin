package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive inspection bundle is in its life cycle. */
enum class AdaptiveInspectionBundleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One adaptive inspection bundle, with what the planners need to act on it. */
data class AdaptiveInspectionBundle(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var dock: String,
    var revision: Int,
    var rate: Double,
    var loadKg: Double,
    var contactEmail: String,
    var ceiling: Double,
    var status: AdaptiveInspectionBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionBundleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionBundleStatus): Boolean {
        when (status) {
            AdaptiveInspectionBundleStatus.CREATED -> return next == AdaptiveInspectionBundleStatus.VALIDATED
            AdaptiveInspectionBundleStatus.VALIDATED -> {
                when (next) {
                    AdaptiveInspectionBundleStatus.PUBLISHED, AdaptiveInspectionBundleStatus.RETIRED -> return true
                }
                return false
            }
            AdaptiveInspectionBundleStatus.PUBLISHED -> return next == AdaptiveInspectionBundleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionBundleStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection bundle $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netExposure(): Double = round((loadKg - ceiling) * 100) / 100

    fun snapshot(): AdaptiveInspectionBundle {
        return AdaptiveInspectionBundle(
            code = code,
            name = name,
            attributes = attributes,
            dock = dock,
            revision = revision,
            rate = rate,
            loadKg = loadKg,
            contactEmail = contactEmail,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive inspection bundle in status created. */
fun newAdaptiveInspectionBundle(code: String, name: String): AdaptiveInspectionBundle {
    return AdaptiveInspectionBundle(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        dock = "",
        revision = 0,
        rate = 0.0,
        loadKg = 0.0,
        contactEmail = "",
        ceiling = 0.0,
        status = AdaptiveInspectionBundleStatus.CREATED,
    )
}
