package com.acme.transport.terminal.dwell.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dwell checkpoint records of terminal's dwell work, and the rules, stores, and calculations around them.

/** Where a dwell checkpoint is in its life cycle. */
enum class DwellCheckpointStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A dwell checkpoint. Its code is unique within a tenant. */
data class DwellCheckpoint(
    var code: String,
    var label: String,
    var externalRef: String,
    var shiftCode: String,
    var tags: MutableList<String>,
    var floorValue: Double,
    var phone: String,
    var contactEmail: String,
    var pinned: Boolean,
    var etaMinutes: Int,
    var description: String,
    var status: DwellCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellCheckpointStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellCheckpointStatus): Boolean {
        when (status) {
            DwellCheckpointStatus.DRAFT -> {
                when (next) {
                    DwellCheckpointStatus.CLOSED -> return true
                }
                return false
            }
            DwellCheckpointStatus.PENDING -> {
                when (next) {
                    DwellCheckpointStatus.CLOSED -> return true
                }
                return false
            }
            DwellCheckpointStatus.ACTIVE -> return next == DwellCheckpointStatus.CLOSED
            DwellCheckpointStatus.SUSPENDED -> {
                when (next) {
                    DwellCheckpointStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("dwell checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"dwell checkpoint"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveExposure(): Double = round((etaMinutes.toDouble() + floorValue) * 100) / 100

    fun snapshot(): DwellCheckpoint {
        return DwellCheckpoint(
            code = code,
            label = label,
            externalRef = externalRef,
            shiftCode = shiftCode,
            tags = tags,
            floorValue = floorValue,
            phone = phone,
            contactEmail = contactEmail,
            pinned = pinned,
            etaMinutes = etaMinutes,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a dwell checkpoint in status draft. */
fun newDwellCheckpoint(code: String, label: String): DwellCheckpoint {
    return DwellCheckpoint(
        code = code,
        label = label,
        externalRef = "",
        shiftCode = "",
        tags = mutableListOf<String>(),
        floorValue = 0.0,
        phone = "",
        contactEmail = "",
        pinned = false,
        etaMinutes = 0,
        description = "",
        status = DwellCheckpointStatus.DRAFT,
    )
}
