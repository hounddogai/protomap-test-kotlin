package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation preference is in its life cycle. */
enum class ViolationPreferenceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a violation preference and its current status. */
data class ViolationPreference(
    var code: String,
    var title: String,
    var segment: String,
    var loadKg: Double,
    var sequence: Int,
    var status: ViolationPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationPreferenceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationPreferenceStatus): Boolean {
        when (status) {
            ViolationPreferenceStatus.IDLE -> return next == ViolationPreferenceStatus.DRAINING
            ViolationPreferenceStatus.WARMING -> return next == ViolationPreferenceStatus.READY
            ViolationPreferenceStatus.READY -> return next == ViolationPreferenceStatus.STOPPED
            ViolationPreferenceStatus.DRAINING -> return next == ViolationPreferenceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ViolationPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("violation preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netTotal(): Double = round((loadKg + sequence.toDouble()) * 100) / 100

    fun snapshot(): ViolationPreference {
        return ViolationPreference(
            code = code,
            title = title,
            segment = segment,
            loadKg = loadKg,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a violation preference in status idle. */
fun newViolationPreference(code: String, title: String): ViolationPreference {
    return ViolationPreference(
        code = code,
        title = title,
        segment = "",
        loadKg = 0.0,
        sequence = 0,
        status = ViolationPreferenceStatus.IDLE,
    )
}
