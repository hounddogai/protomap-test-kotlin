package com.acme.transport.safety.incident.digest

import com.acme.transport.kit.text.normalize

/** Where a tiered incident preference is in its life cycle. */
enum class TieredIncidentPreferenceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One tiered incident preference, with what the planners need to act on it. */
data class TieredIncidentPreference(
    var code: String,
    var label: String,
    var country: String,
    var driverId: String,
    var score: Double,
    var name: String,
    var pinned: Boolean,
    var status: TieredIncidentPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredIncidentPreferenceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredIncidentPreferenceStatus): Boolean {
        when (status) {
            TieredIncidentPreferenceStatus.NEW -> {
                when (next) {
                    TieredIncidentPreferenceStatus.QUEUED, TieredIncidentPreferenceStatus.DONE -> return true
                }
                return false
            }
            TieredIncidentPreferenceStatus.QUEUED -> {
                when (next) {
                    TieredIncidentPreferenceStatus.DONE, TieredIncidentPreferenceStatus.FAILED -> return true
                }
                return false
            }
            TieredIncidentPreferenceStatus.RUNNING -> {
                when (next) {
                    TieredIncidentPreferenceStatus.FAILED -> return true
                }
                return false
            }
            TieredIncidentPreferenceStatus.DONE -> {
                when (next) {
                    TieredIncidentPreferenceStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredIncidentPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("tiered incident preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = score

    fun snapshot(): TieredIncidentPreference {
        return TieredIncidentPreference(
            code = code,
            label = label,
            country = country,
            driverId = driverId,
            score = score,
            name = name,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tiered incident preference in status new. */
fun newTieredIncidentPreference(code: String, label: String): TieredIncidentPreference {
    return TieredIncidentPreference(
        code = code,
        label = label,
        country = "",
        driverId = "",
        score = 0.0,
        name = "",
        pinned = false,
        status = TieredIncidentPreferenceStatus.NEW,
    )
}
