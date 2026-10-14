package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy emission factor preference is in its life cycle. */
enum class LegacyEmissionFactorPreferenceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One legacy emission factor preference, with what the planners need to act on it. */
data class LegacyEmissionFactorPreference(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var rate: Double,
    var tags: MutableList<String>,
    var minimum: Double,
    var name: String,
    var threshold: Double,
    var startsAt: Int,
    var score: Double,
    var batchSize: Int,
    var status: LegacyEmissionFactorPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyEmissionFactorPreferenceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyEmissionFactorPreferenceStatus): Boolean {
        when (status) {
            LegacyEmissionFactorPreferenceStatus.NEW -> return next == LegacyEmissionFactorPreferenceStatus.DONE
            LegacyEmissionFactorPreferenceStatus.QUEUED -> {
                when (next) {
                    LegacyEmissionFactorPreferenceStatus.DONE -> return true
                }
                return false
            }
            LegacyEmissionFactorPreferenceStatus.RUNNING -> {
                when (next) {
                    LegacyEmissionFactorPreferenceStatus.DONE, LegacyEmissionFactorPreferenceStatus.FAILED -> return true
                }
                return false
            }
            LegacyEmissionFactorPreferenceStatus.DONE -> {
                when (next) {
                    LegacyEmissionFactorPreferenceStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyEmissionFactorPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("legacy emission factor preference $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

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

    fun effectiveLoad(): Double = round((score + startsAt.toDouble()) * 100) / 100

    fun snapshot(): LegacyEmissionFactorPreference {
        return LegacyEmissionFactorPreference(
            code = code,
            title = title,
            attributes = attributes,
            rate = rate,
            tags = tags,
            minimum = minimum,
            name = name,
            threshold = threshold,
            startsAt = startsAt,
            score = score,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy emission factor preference in status new. */
fun newLegacyEmissionFactorPreference(code: String, title: String): LegacyEmissionFactorPreference {
    return LegacyEmissionFactorPreference(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        rate = 0.0,
        tags = mutableListOf<String>(),
        minimum = 0.0,
        name = "",
        threshold = 0.0,
        startsAt = 0,
        score = 0.0,
        batchSize = 0,
        status = LegacyEmissionFactorPreferenceStatus.NEW,
    )
}
