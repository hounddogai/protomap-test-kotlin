package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.kit.text.normalize

/** Where a adaptive inspection manifest is in its life cycle. */
enum class AdaptiveInspectionManifestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a adaptive inspection manifest and its current status. */
data class AdaptiveInspectionManifest(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var ownerId: String,
    var source: String,
    var quantity: Int,
    var status: AdaptiveInspectionManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionManifestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionManifestStatus): Boolean {
        when (status) {
            AdaptiveInspectionManifestStatus.OPEN -> return next == AdaptiveInspectionManifestStatus.REOPENED || next == AdaptiveInspectionManifestStatus.ARCHIVED
            AdaptiveInspectionManifestStatus.IN_REVIEW -> {
                when (next) {
                    AdaptiveInspectionManifestStatus.RESOLVED, AdaptiveInspectionManifestStatus.REOPENED -> return true
                }
                return false
            }
            AdaptiveInspectionManifestStatus.RESOLVED -> return next == AdaptiveInspectionManifestStatus.REOPENED || next == AdaptiveInspectionManifestStatus.ARCHIVED
            AdaptiveInspectionManifestStatus.REOPENED -> return next == AdaptiveInspectionManifestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionManifestStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): AdaptiveInspectionManifest {
        return AdaptiveInspectionManifest(
            code = code,
            title = title,
            attributes = attributes,
            ownerId = ownerId,
            source = source,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a adaptive inspection manifest in status open. */
fun newAdaptiveInspectionManifest(code: String, title: String): AdaptiveInspectionManifest {
    return AdaptiveInspectionManifest(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        ownerId = "",
        source = "",
        quantity = 0,
        status = AdaptiveInspectionManifestStatus.OPEN,
    )
}
