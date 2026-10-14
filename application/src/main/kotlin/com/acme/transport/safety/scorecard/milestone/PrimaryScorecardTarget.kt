package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary scorecard target is in its life cycle. */
enum class PrimaryScorecardTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A primary scorecard target. Its code is unique within a tenant. */
data class PrimaryScorecardTarget(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var score: Double,
    var etaMinutes: Int,
    var status: PrimaryScorecardTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryScorecardTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryScorecardTargetStatus): Boolean {
        when (status) {
            PrimaryScorecardTargetStatus.CREATED -> {
                when (next) {
                    PrimaryScorecardTargetStatus.VALIDATED, PrimaryScorecardTargetStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryScorecardTargetStatus.VALIDATED -> {
                when (next) {
                    PrimaryScorecardTargetStatus.PUBLISHED, PrimaryScorecardTargetStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryScorecardTargetStatus.PUBLISHED -> {
                when (next) {
                    PrimaryScorecardTargetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryScorecardTargetStatus) {
        if (!canMoveTo(next)) {
            error("primary scorecard target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary scorecard target"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netTotal(): Double = round((score + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): PrimaryScorecardTarget {
        return PrimaryScorecardTarget(
            code = code,
            label = label,
            attributes = attributes,
            score = score,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary scorecard target in status created. */
fun newPrimaryScorecardTarget(code: String, label: String): PrimaryScorecardTarget {
    return PrimaryScorecardTarget(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        score = 0.0,
        etaMinutes = 0,
        status = PrimaryScorecardTargetStatus.CREATED,
    )
}
