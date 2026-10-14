package com.acme.transport.network.node.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional node checkpoint is in its life cycle. */
enum class RegionalNodeCheckpointStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A regional node checkpoint. Its code is unique within a tenant. */
data class RegionalNodeCheckpoint(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var ratio: Double,
    var trailerId: String,
    var locale: String,
    var durationMinutes: Int,
    var budget: Double,
    var status: RegionalNodeCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalNodeCheckpointStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalNodeCheckpointStatus): Boolean {
        when (status) {
            RegionalNodeCheckpointStatus.OPEN -> {
                when (next) {
                    RegionalNodeCheckpointStatus.REOPENED, RegionalNodeCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalNodeCheckpointStatus.IN_REVIEW -> {
                when (next) {
                    RegionalNodeCheckpointStatus.RESOLVED, RegionalNodeCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalNodeCheckpointStatus.RESOLVED -> {
                when (next) {
                    RegionalNodeCheckpointStatus.REOPENED, RegionalNodeCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalNodeCheckpointStatus.REOPENED -> return next == RegionalNodeCheckpointStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RegionalNodeCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("regional node checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netLoad(): Double = round(durationMinutes.toDouble() * budget * 100) / 100

    fun snapshot(): RegionalNodeCheckpoint {
        return RegionalNodeCheckpoint(
            code = code,
            title = title,
            tags = tags,
            ratio = ratio,
            trailerId = trailerId,
            locale = locale,
            durationMinutes = durationMinutes,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a regional node checkpoint in status open. */
fun newRegionalNodeCheckpoint(code: String, title: String): RegionalNodeCheckpoint {
    return RegionalNodeCheckpoint(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        ratio = 0.0,
        trailerId = "",
        locale = "",
        durationMinutes = 0,
        budget = 0.0,
        status = RegionalNodeCheckpointStatus.OPEN,
    )
}
