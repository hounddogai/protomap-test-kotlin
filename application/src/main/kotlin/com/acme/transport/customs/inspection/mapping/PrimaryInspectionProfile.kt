package com.acme.transport.customs.inspection.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary inspection profile is in its life cycle. */
enum class PrimaryInspectionProfileStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A primary inspection profile. Its code is unique within a tenant. */
data class PrimaryInspectionProfile(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var pinned: Boolean,
    var durationMinutes: Int,
    var stopCount: Int,
    var status: PrimaryInspectionProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryInspectionProfileStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryInspectionProfileStatus): Boolean {
        when (status) {
            PrimaryInspectionProfileStatus.NEW -> return next == PrimaryInspectionProfileStatus.QUEUED || next == PrimaryInspectionProfileStatus.FAILED
            PrimaryInspectionProfileStatus.QUEUED -> {
                when (next) {
                    PrimaryInspectionProfileStatus.RUNNING, PrimaryInspectionProfileStatus.FAILED -> return true
                }
                return false
            }
            PrimaryInspectionProfileStatus.RUNNING -> {
                when (next) {
                    PrimaryInspectionProfileStatus.DONE, PrimaryInspectionProfileStatus.FAILED -> return true
                }
                return false
            }
            PrimaryInspectionProfileStatus.DONE -> {
                when (next) {
                    PrimaryInspectionProfileStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryInspectionProfileStatus) {
        if (!canMoveTo(next)) {
            error("primary inspection profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary inspection profile"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedExposure(): Double = round((durationMinutes.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): PrimaryInspectionProfile {
        return PrimaryInspectionProfile(
            code = code,
            label = label,
            attributes = attributes,
            pinned = pinned,
            durationMinutes = durationMinutes,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary inspection profile in status new. */
fun newPrimaryInspectionProfile(code: String, label: String): PrimaryInspectionProfile {
    return PrimaryInspectionProfile(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        pinned = false,
        durationMinutes = 0,
        stopCount = 0,
        status = PrimaryInspectionProfileStatus.NEW,
    )
}
