package com.acme.transport.freightpay.accessorial.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The accessorial manifest records of freightpay's accessorial work, and the rules, stores, and calculations
// around them.

/** Where a accessorial manifest is in its life cycle. */
enum class AccessorialManifestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One accessorial manifest, with what the planners need to act on it. */
data class AccessorialManifest(
    var code: String,
    var title: String,
    var driverId: String,
    var summary: String,
    var margin: Double,
    var attributes: MutableMap<String, String>,
    var notes: String,
    var label: String,
    var graceDays: Int,
    var status: AccessorialManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialManifestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialManifestStatus): Boolean {
        when (status) {
            AccessorialManifestStatus.REQUESTED -> {
                when (next) {
                    AccessorialManifestStatus.REVOKED -> return true
                }
                return false
            }
            AccessorialManifestStatus.GRANTED -> {
                when (next) {
                    AccessorialManifestStatus.REVOKED, AccessorialManifestStatus.EXPIRED -> return true
                }
                return false
            }
            AccessorialManifestStatus.REVOKED -> return next == AccessorialManifestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AccessorialManifestStatus) {
        if (!canMoveTo(next)) {
            error("accessorial manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedValue(): Double = round((graceDays.toDouble() - margin) * 100) / 100

    fun snapshot(): AccessorialManifest {
        return AccessorialManifest(
            code = code,
            title = title,
            driverId = driverId,
            summary = summary,
            margin = margin,
            attributes = attributes,
            notes = notes,
            label = label,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a accessorial manifest in status requested. */
fun newAccessorialManifest(code: String, title: String): AccessorialManifest {
    return AccessorialManifest(
        code = code,
        title = title,
        driverId = "",
        summary = "",
        margin = 0.0,
        attributes = mutableMapOf<String, String>(),
        notes = "",
        label = "",
        graceDays = 0,
        status = AccessorialManifestStatus.REQUESTED,
    )
}
