package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global backhaul preference is in its life cycle. */
enum class GlobalBackhaulPreferenceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One global backhaul preference, with what the planners need to act on it. */
data class GlobalBackhaulPreference(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var endsAt: Int,
    var priority: Int,
    var visible: Boolean,
    var externalRef: String,
    var revision: Int,
    var status: GlobalBackhaulPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalBackhaulPreferenceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalBackhaulPreferenceStatus): Boolean {
        when (status) {
            GlobalBackhaulPreferenceStatus.PLANNED -> {
                when (next) {
                    GlobalBackhaulPreferenceStatus.SCHEDULED, GlobalBackhaulPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalBackhaulPreferenceStatus.SCHEDULED -> return next == GlobalBackhaulPreferenceStatus.CANCELLED
            GlobalBackhaulPreferenceStatus.IN_PROGRESS -> return next == GlobalBackhaulPreferenceStatus.COMPLETED || next == GlobalBackhaulPreferenceStatus.CANCELLED
            GlobalBackhaulPreferenceStatus.COMPLETED -> {
                when (next) {
                    GlobalBackhaulPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalBackhaulPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("global backhaul preference $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"global backhaul preference"} $code"

    fun netTotal(): Double = round((revision.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): GlobalBackhaulPreference {
        return GlobalBackhaulPreference(
            code = code,
            label = label,
            expiresAt = expiresAt,
            endsAt = endsAt,
            priority = priority,
            visible = visible,
            externalRef = externalRef,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global backhaul preference in status planned. */
fun newGlobalBackhaulPreference(code: String, label: String): GlobalBackhaulPreference {
    return GlobalBackhaulPreference(
        code = code,
        label = label,
        expiresAt = 0,
        endsAt = 0,
        priority = 0,
        visible = false,
        externalRef = "",
        revision = 0,
        status = GlobalBackhaulPreferenceStatus.PLANNED,
    )
}
