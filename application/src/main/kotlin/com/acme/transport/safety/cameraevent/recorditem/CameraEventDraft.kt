package com.acme.transport.safety.cameraevent.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event draft is in its life cycle. */
enum class CameraEventDraftStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One camera event draft, with what the planners need to act on it. */
data class CameraEventDraft(
    var code: String,
    var label: String,
    var sequence: Int,
    var margin: Double,
    var name: String,
    var score: Double,
    var status: CameraEventDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventDraftStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventDraftStatus): Boolean {
        when (status) {
            CameraEventDraftStatus.OPEN -> return next == CameraEventDraftStatus.IN_REVIEW || next == CameraEventDraftStatus.ARCHIVED
            CameraEventDraftStatus.IN_REVIEW -> {
                when (next) {
                    CameraEventDraftStatus.ARCHIVED -> return true
                }
                return false
            }
            CameraEventDraftStatus.RESOLVED -> return next == CameraEventDraftStatus.ARCHIVED
            CameraEventDraftStatus.REOPENED -> return next == CameraEventDraftStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CameraEventDraftStatus) {
        if (!canMoveTo(next)) {
            error("camera event draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"camera event draft"} $code"

    fun effectiveVolume(): Double = round((margin - sequence.toDouble()) * 100) / 100

    fun snapshot(): CameraEventDraft {
        return CameraEventDraft(
            code = code,
            label = label,
            sequence = sequence,
            margin = margin,
            name = name,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a camera event draft in status open. */
fun newCameraEventDraft(code: String, label: String): CameraEventDraft {
    return CameraEventDraft(
        code = code,
        label = label,
        sequence = 0,
        margin = 0.0,
        name = "",
        score = 0.0,
        status = CameraEventDraftStatus.OPEN,
    )
}
