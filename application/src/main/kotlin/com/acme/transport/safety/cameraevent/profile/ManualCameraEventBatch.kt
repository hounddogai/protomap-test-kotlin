package com.acme.transport.safety.cameraevent.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual camera event batch is in its life cycle. */
enum class ManualCameraEventBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a manual camera event batch and its current status. */
data class ManualCameraEventBatch(
    var code: String,
    var title: String,
    var margin: Double,
    var startsAt: Int,
    var cost: Double,
    var createdAt: Int,
    var etaMinutes: Int,
    var archived: Boolean,
    var weight: Double,
    var attributes: MutableMap<String, String>,
    var status: ManualCameraEventBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCameraEventBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCameraEventBatchStatus): Boolean {
        when (status) {
            ManualCameraEventBatchStatus.OPEN -> return next == ManualCameraEventBatchStatus.RESOLVED || next == ManualCameraEventBatchStatus.REOPENED
            ManualCameraEventBatchStatus.IN_REVIEW -> return next == ManualCameraEventBatchStatus.REOPENED || next == ManualCameraEventBatchStatus.ARCHIVED
            ManualCameraEventBatchStatus.RESOLVED -> {
                when (next) {
                    ManualCameraEventBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualCameraEventBatchStatus.REOPENED -> {
                when (next) {
                    ManualCameraEventBatchStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualCameraEventBatchStatus) {
        if (!canMoveTo(next)) {
            error("manual camera event batch $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedVolume(): Double = round((etaMinutes.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): ManualCameraEventBatch {
        return ManualCameraEventBatch(
            code = code,
            title = title,
            margin = margin,
            startsAt = startsAt,
            cost = cost,
            createdAt = createdAt,
            etaMinutes = etaMinutes,
            archived = archived,
            weight = weight,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual camera event batch in status open. */
fun newManualCameraEventBatch(code: String, title: String): ManualCameraEventBatch {
    return ManualCameraEventBatch(
        code = code,
        title = title,
        margin = 0.0,
        startsAt = 0,
        cost = 0.0,
        createdAt = 0,
        etaMinutes = 0,
        archived = false,
        weight = 0.0,
        attributes = mutableMapOf<String, String>(),
        status = ManualCameraEventBatchStatus.OPEN,
    )
}
