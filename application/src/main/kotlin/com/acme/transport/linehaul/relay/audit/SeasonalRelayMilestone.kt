package com.acme.transport.linehaul.relay.audit

import com.acme.transport.kit.text.normalize

/** Where a seasonal relay milestone is in its life cycle. */
enum class SeasonalRelayMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal relay milestone. Its code is unique within a tenant. */
data class SeasonalRelayMilestone(
    var code: String,
    var name: String,
    var attempts: Int,
    var attributes: MutableMap<String, String>,
    var source: String,
    var title: String,
    var status: SeasonalRelayMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRelayMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRelayMilestoneStatus): Boolean {
        when (status) {
            SeasonalRelayMilestoneStatus.NEW -> {
                when (next) {
                    SeasonalRelayMilestoneStatus.RUNNING, SeasonalRelayMilestoneStatus.DONE -> return true
                }
                return false
            }
            SeasonalRelayMilestoneStatus.QUEUED -> {
                when (next) {
                    SeasonalRelayMilestoneStatus.RUNNING -> return true
                }
                return false
            }
            SeasonalRelayMilestoneStatus.RUNNING -> {
                when (next) {
                    SeasonalRelayMilestoneStatus.DONE -> return true
                }
                return false
            }
            SeasonalRelayMilestoneStatus.DONE -> {
                when (next) {
                    SeasonalRelayMilestoneStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalRelayMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("seasonal relay milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): SeasonalRelayMilestone {
        return SeasonalRelayMilestone(
            code = code,
            name = name,
            attempts = attempts,
            attributes = attributes,
            source = source,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a seasonal relay milestone in status new. */
fun newSeasonalRelayMilestone(code: String, name: String): SeasonalRelayMilestone {
    return SeasonalRelayMilestone(
        code = code,
        name = name,
        attempts = 0,
        attributes = mutableMapOf<String, String>(),
        source = "",
        title = "",
        status = SeasonalRelayMilestoneStatus.NEW,
    )
}
