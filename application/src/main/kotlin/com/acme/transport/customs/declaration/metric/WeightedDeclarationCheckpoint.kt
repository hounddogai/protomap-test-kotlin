package com.acme.transport.customs.declaration.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted declaration checkpoint is in its life cycle. */
enum class WeightedDeclarationCheckpointStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One weighted declaration checkpoint, with what the planners need to act on it. */
data class WeightedDeclarationCheckpoint(
    var code: String,
    var name: String,
    var revision: Int,
    var visible: Boolean,
    var lane: String,
    var externalRef: String,
    var archived: Boolean,
    var windowDays: Int,
    var shiftCode: String,
    var carrier: String,
    var status: WeightedDeclarationCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDeclarationCheckpointStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDeclarationCheckpointStatus): Boolean {
        when (status) {
            WeightedDeclarationCheckpointStatus.OPEN -> return next == WeightedDeclarationCheckpointStatus.IN_REVIEW || next == WeightedDeclarationCheckpointStatus.RESOLVED
            WeightedDeclarationCheckpointStatus.IN_REVIEW -> return next == WeightedDeclarationCheckpointStatus.ARCHIVED
            WeightedDeclarationCheckpointStatus.RESOLVED -> return next == WeightedDeclarationCheckpointStatus.REOPENED
            WeightedDeclarationCheckpointStatus.REOPENED -> {
                when (next) {
                    WeightedDeclarationCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedDeclarationCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("weighted declaration checkpoint $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"weighted declaration checkpoint"} $code"

    fun adjustedValue(): Double = round((revision.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): WeightedDeclarationCheckpoint {
        return WeightedDeclarationCheckpoint(
            code = code,
            name = name,
            revision = revision,
            visible = visible,
            lane = lane,
            externalRef = externalRef,
            archived = archived,
            windowDays = windowDays,
            shiftCode = shiftCode,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a weighted declaration checkpoint in status open. */
fun newWeightedDeclarationCheckpoint(code: String, name: String): WeightedDeclarationCheckpoint {
    return WeightedDeclarationCheckpoint(
        code = code,
        name = name,
        revision = 0,
        visible = false,
        lane = "",
        externalRef = "",
        archived = false,
        windowDays = 0,
        shiftCode = "",
        carrier = "",
        status = WeightedDeclarationCheckpointStatus.OPEN,
    )
}
