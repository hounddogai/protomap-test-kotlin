package com.acme.transport.emissions.idling.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling bundle is in its life cycle. */
enum class IdlingBundleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One idling bundle, with what the planners need to act on it. */
data class IdlingBundle(
    var code: String,
    var label: String,
    var lane: String,
    var summary: String,
    var approved: Boolean,
    var source: String,
    var revision: Int,
    var createdAt: Int,
    var status: IdlingBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingBundleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingBundleStatus): Boolean {
        when (status) {
            IdlingBundleStatus.DRAFT -> return next == IdlingBundleStatus.ACTIVE || next == IdlingBundleStatus.CLOSED
            IdlingBundleStatus.PENDING -> {
                when (next) {
                    IdlingBundleStatus.ACTIVE, IdlingBundleStatus.CLOSED -> return true
                }
                return false
            }
            IdlingBundleStatus.ACTIVE -> return next == IdlingBundleStatus.SUSPENDED
            IdlingBundleStatus.SUSPENDED -> {
                when (next) {
                    IdlingBundleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingBundleStatus) {
        if (!canMoveTo(next)) {
            error("idling bundle $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedLoad(): Double = round((revision.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): IdlingBundle {
        return IdlingBundle(
            code = code,
            label = label,
            lane = lane,
            summary = summary,
            approved = approved,
            source = source,
            revision = revision,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a idling bundle in status draft. */
fun newIdlingBundle(code: String, label: String): IdlingBundle {
    return IdlingBundle(
        code = code,
        label = label,
        lane = "",
        summary = "",
        approved = false,
        source = "",
        revision = 0,
        createdAt = 0,
        status = IdlingBundleStatus.DRAFT,
    )
}
