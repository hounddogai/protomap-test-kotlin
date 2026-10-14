package com.acme.transport.linehaul.laneplan.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy lane plan target is in its life cycle. */
enum class LegacyLanePlanTargetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a legacy lane plan target and its current status. */
data class LegacyLanePlanTarget(
    var code: String,
    var label: String,
    var weight: Double,
    var sequence: Int,
    var approved: Boolean,
    var attributes: MutableMap<String, String>,
    var segment: String,
    var tags: MutableList<String>,
    var status: LegacyLanePlanTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLanePlanTargetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLanePlanTargetStatus): Boolean {
        when (status) {
            LegacyLanePlanTargetStatus.REQUESTED -> return next == LegacyLanePlanTargetStatus.GRANTED || next == LegacyLanePlanTargetStatus.EXPIRED
            LegacyLanePlanTargetStatus.GRANTED -> {
                when (next) {
                    LegacyLanePlanTargetStatus.REVOKED, LegacyLanePlanTargetStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyLanePlanTargetStatus.REVOKED -> return next == LegacyLanePlanTargetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LegacyLanePlanTargetStatus) {
        if (!canMoveTo(next)) {
            error("legacy lane plan target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy lane plan target"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round((sequence.toDouble() - weight) * 100) / 100

    fun snapshot(): LegacyLanePlanTarget {
        return LegacyLanePlanTarget(
            code = code,
            label = label,
            weight = weight,
            sequence = sequence,
            approved = approved,
            attributes = attributes,
            segment = segment,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a legacy lane plan target in status requested. */
fun newLegacyLanePlanTarget(code: String, label: String): LegacyLanePlanTarget {
    return LegacyLanePlanTarget(
        code = code,
        label = label,
        weight = 0.0,
        sequence = 0,
        approved = false,
        attributes = mutableMapOf<String, String>(),
        segment = "",
        tags = mutableListOf<String>(),
        status = LegacyLanePlanTargetStatus.REQUESTED,
    )
}
