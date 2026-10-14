package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped lane plan target is in its life cycle. */
enum class CappedLanePlanTargetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a capped lane plan target and its current status. */
data class CappedLanePlanTarget(
    var code: String,
    var label: String,
    var score: Double,
    var endsAt: Int,
    var rate: Double,
    var capacity: Int,
    var archived: Boolean,
    var checksum: String,
    var status: CappedLanePlanTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedLanePlanTargetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedLanePlanTargetStatus): Boolean {
        when (status) {
            CappedLanePlanTargetStatus.NEW -> return next == CappedLanePlanTargetStatus.QUEUED || next == CappedLanePlanTargetStatus.FAILED
            CappedLanePlanTargetStatus.QUEUED -> {
                when (next) {
                    CappedLanePlanTargetStatus.RUNNING, CappedLanePlanTargetStatus.DONE -> return true
                }
                return false
            }
            CappedLanePlanTargetStatus.RUNNING -> return next == CappedLanePlanTargetStatus.DONE || next == CappedLanePlanTargetStatus.FAILED
            CappedLanePlanTargetStatus.DONE -> return next == CappedLanePlanTargetStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CappedLanePlanTargetStatus) {
        if (!canMoveTo(next)) {
            error("capped lane plan target $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveLoad(): Double = round((rate - score) * 100) / 100

    fun snapshot(): CappedLanePlanTarget {
        return CappedLanePlanTarget(
            code = code,
            label = label,
            score = score,
            endsAt = endsAt,
            rate = rate,
            capacity = capacity,
            archived = archived,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped lane plan target in status new. */
fun newCappedLanePlanTarget(code: String, label: String): CappedLanePlanTarget {
    return CappedLanePlanTarget(
        code = code,
        label = label,
        score = 0.0,
        endsAt = 0,
        rate = 0.0,
        capacity = 0,
        archived = false,
        checksum = "",
        status = CappedLanePlanTargetStatus.NEW,
    )
}
