package com.acme.transport.network.hub.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted hub draft is in its life cycle. */
enum class WeightedHubDraftStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted hub draft. Its code is unique within a tenant. */
data class WeightedHubDraft(
    var code: String,
    var label: String,
    var title: String,
    var version: Int,
    var trailerId: String,
    var margin: Double,
    var archived: Boolean,
    var checksum: String,
    var status: WeightedHubDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedHubDraftStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedHubDraftStatus): Boolean {
        when (status) {
            WeightedHubDraftStatus.PLANNED -> return next == WeightedHubDraftStatus.SCHEDULED
            WeightedHubDraftStatus.SCHEDULED -> return next == WeightedHubDraftStatus.IN_PROGRESS || next == WeightedHubDraftStatus.CANCELLED
            WeightedHubDraftStatus.IN_PROGRESS -> return next == WeightedHubDraftStatus.CANCELLED
            WeightedHubDraftStatus.COMPLETED -> {
                when (next) {
                    WeightedHubDraftStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedHubDraftStatus) {
        if (!canMoveTo(next)) {
            error("weighted hub draft $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedTotal(): Double = round(margin * version.toDouble() * 100) / 100

    fun snapshot(): WeightedHubDraft {
        return WeightedHubDraft(
            code = code,
            label = label,
            title = title,
            version = version,
            trailerId = trailerId,
            margin = margin,
            archived = archived,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted hub draft in status planned. */
fun newWeightedHubDraft(code: String, label: String): WeightedHubDraft {
    return WeightedHubDraft(
        code = code,
        label = label,
        title = "",
        version = 0,
        trailerId = "",
        margin = 0.0,
        archived = false,
        checksum = "",
        status = WeightedHubDraftStatus.PLANNED,
    )
}
