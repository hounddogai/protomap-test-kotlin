package com.acme.transport.linehaul.laneplan.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy lane plan manifest records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a legacy lane plan manifest is in its life cycle. */
enum class LegacyLanePlanManifestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One legacy lane plan manifest, with what the planners need to act on it. */
data class LegacyLanePlanManifest(
    var code: String,
    var label: String,
    var loadKg: Double,
    var batchSize: Int,
    var timezone: String,
    var revision: Int,
    var segment: String,
    var status: LegacyLanePlanManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLanePlanManifestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLanePlanManifestStatus): Boolean {
        when (status) {
            LegacyLanePlanManifestStatus.OPEN -> {
                when (next) {
                    LegacyLanePlanManifestStatus.IN_REVIEW, LegacyLanePlanManifestStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyLanePlanManifestStatus.IN_REVIEW -> {
                when (next) {
                    LegacyLanePlanManifestStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyLanePlanManifestStatus.RESOLVED -> {
                when (next) {
                    LegacyLanePlanManifestStatus.REOPENED, LegacyLanePlanManifestStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyLanePlanManifestStatus.REOPENED -> {
                when (next) {
                    LegacyLanePlanManifestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyLanePlanManifestStatus) {
        if (!canMoveTo(next)) {
            error("legacy lane plan manifest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"legacy lane plan manifest"} $code"

    fun effectiveValue(): Double = round(batchSize.toDouble() * loadKg * 100) / 100

    fun snapshot(): LegacyLanePlanManifest {
        return LegacyLanePlanManifest(
            code = code,
            label = label,
            loadKg = loadKg,
            batchSize = batchSize,
            timezone = timezone,
            revision = revision,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a legacy lane plan manifest in status open. */
fun newLegacyLanePlanManifest(code: String, label: String): LegacyLanePlanManifest {
    return LegacyLanePlanManifest(
        code = code,
        label = label,
        loadKg = 0.0,
        batchSize = 0,
        timezone = "",
        revision = 0,
        segment = "",
        status = LegacyLanePlanManifestStatus.OPEN,
    )
}
