package com.acme.transport.network.transittime.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global transit time template is in its life cycle. */
enum class GlobalTransitTimeTemplateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A global transit time template. Its code is unique within a tenant. */
data class GlobalTransitTimeTemplate(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var visible: Boolean,
    var startsAt: Int,
    var ownerId: String,
    var stopCount: Int,
    var durationMinutes: Int,
    var graceDays: Int,
    var attempts: Int,
    var status: GlobalTransitTimeTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTransitTimeTemplateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTransitTimeTemplateStatus): Boolean {
        when (status) {
            GlobalTransitTimeTemplateStatus.OPEN -> return next == GlobalTransitTimeTemplateStatus.IN_REVIEW || next == GlobalTransitTimeTemplateStatus.RESOLVED
            GlobalTransitTimeTemplateStatus.IN_REVIEW -> {
                when (next) {
                    GlobalTransitTimeTemplateStatus.RESOLVED, GlobalTransitTimeTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalTransitTimeTemplateStatus.RESOLVED -> {
                when (next) {
                    GlobalTransitTimeTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalTransitTimeTemplateStatus.REOPENED -> return next == GlobalTransitTimeTemplateStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalTransitTimeTemplateStatus) {
        if (!canMoveTo(next)) {
            error("global transit time template $code cannot move from $status to $next")
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

    fun weightedTotal(): Double = round(durationMinutes.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): GlobalTransitTimeTemplate {
        return GlobalTransitTimeTemplate(
            code = code,
            title = title,
            tags = tags,
            visible = visible,
            startsAt = startsAt,
            ownerId = ownerId,
            stopCount = stopCount,
            durationMinutes = durationMinutes,
            graceDays = graceDays,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global transit time template in status open. */
fun newGlobalTransitTimeTemplate(code: String, title: String): GlobalTransitTimeTemplate {
    return GlobalTransitTimeTemplate(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        visible = false,
        startsAt = 0,
        ownerId = "",
        stopCount = 0,
        durationMinutes = 0,
        graceDays = 0,
        attempts = 0,
        status = GlobalTransitTimeTemplateStatus.OPEN,
    )
}
