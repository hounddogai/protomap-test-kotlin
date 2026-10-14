package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual tender preference is in its life cycle. */
enum class ManualTenderPreferenceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One manual tender preference, with what the planners need to act on it. */
data class ManualTenderPreference(
    var code: String,
    var label: String,
    var ratio: Double,
    var distanceKm: Double,
    var startsAt: Int,
    var cost: Double,
    var budget: Double,
    var description: String,
    var revision: Int,
    var attributes: MutableMap<String, String>,
    var checksum: String,
    var status: ManualTenderPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTenderPreferenceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTenderPreferenceStatus): Boolean {
        when (status) {
            ManualTenderPreferenceStatus.CREATED -> {
                when (next) {
                    ManualTenderPreferenceStatus.PUBLISHED, ManualTenderPreferenceStatus.RETIRED -> return true
                }
                return false
            }
            ManualTenderPreferenceStatus.VALIDATED -> {
                when (next) {
                    ManualTenderPreferenceStatus.PUBLISHED, ManualTenderPreferenceStatus.RETIRED -> return true
                }
                return false
            }
            ManualTenderPreferenceStatus.PUBLISHED -> {
                when (next) {
                    ManualTenderPreferenceStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualTenderPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("manual tender preference $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"manual tender preference"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedExposure(): Double = round((budget - cost) * 100) / 100

    fun snapshot(): ManualTenderPreference {
        return ManualTenderPreference(
            code = code,
            label = label,
            ratio = ratio,
            distanceKm = distanceKm,
            startsAt = startsAt,
            cost = cost,
            budget = budget,
            description = description,
            revision = revision,
            attributes = attributes,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual tender preference in status created. */
fun newManualTenderPreference(code: String, label: String): ManualTenderPreference {
    return ManualTenderPreference(
        code = code,
        label = label,
        ratio = 0.0,
        distanceKm = 0.0,
        startsAt = 0,
        cost = 0.0,
        budget = 0.0,
        description = "",
        revision = 0,
        attributes = mutableMapOf<String, String>(),
        checksum = "",
        status = ManualTenderPreferenceStatus.CREATED,
    )
}
