package com.acme.transport.freightpay.audit.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit checkpoint is in its life cycle. */
enum class AuditCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a audit checkpoint and its current status. */
data class AuditCheckpoint(
    var code: String,
    var name: String,
    var segment: String,
    var priority: Int,
    var etaMinutes: Int,
    var shiftCode: String,
    var score: Double,
    var tags: MutableList<String>,
    var notes: String,
    var status: AuditCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditCheckpointStatus): Boolean {
        when (status) {
            AuditCheckpointStatus.IDLE -> return next == AuditCheckpointStatus.WARMING
            AuditCheckpointStatus.WARMING -> {
                when (next) {
                    AuditCheckpointStatus.READY -> return true
                }
                return false
            }
            AuditCheckpointStatus.READY -> return next == AuditCheckpointStatus.DRAINING || next == AuditCheckpointStatus.STOPPED
            AuditCheckpointStatus.DRAINING -> return next == AuditCheckpointStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AuditCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("audit checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedValue(): Double = round((etaMinutes.toDouble() - score) * 100) / 100

    fun snapshot(): AuditCheckpoint {
        return AuditCheckpoint(
            code = code,
            name = name,
            segment = segment,
            priority = priority,
            etaMinutes = etaMinutes,
            shiftCode = shiftCode,
            score = score,
            tags = tags,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a audit checkpoint in status idle. */
fun newAuditCheckpoint(code: String, name: String): AuditCheckpoint {
    return AuditCheckpoint(
        code = code,
        name = name,
        segment = "",
        priority = 0,
        etaMinutes = 0,
        shiftCode = "",
        score = 0.0,
        tags = mutableListOf<String>(),
        notes = "",
        status = AuditCheckpointStatus.IDLE,
    )
}
