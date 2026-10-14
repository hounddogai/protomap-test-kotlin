package com.acme.transport.emissions.idling.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient idling slot is in its life cycle. */
enum class LenientIdlingSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A lenient idling slot. Its code is unique within a tenant. */
data class LenientIdlingSlot(
    var code: String,
    var label: String,
    var archived: Boolean,
    var minimum: Double,
    var notes: String,
    var version: Int,
    var dock: String,
    var revision: Int,
    var distanceKm: Double,
    var externalRef: String,
    var status: LenientIdlingSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientIdlingSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientIdlingSlotStatus): Boolean {
        when (status) {
            LenientIdlingSlotStatus.OPEN -> {
                when (next) {
                    LenientIdlingSlotStatus.IN_REVIEW, LenientIdlingSlotStatus.REOPENED -> return true
                }
                return false
            }
            LenientIdlingSlotStatus.IN_REVIEW -> {
                when (next) {
                    LenientIdlingSlotStatus.RESOLVED -> return true
                }
                return false
            }
            LenientIdlingSlotStatus.RESOLVED -> {
                when (next) {
                    LenientIdlingSlotStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientIdlingSlotStatus.REOPENED -> {
                when (next) {
                    LenientIdlingSlotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientIdlingSlotStatus) {
        if (!canMoveTo(next)) {
            error("lenient idling slot $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"lenient idling slot"} $code"

    fun projectedVolume(): Double = round((minimum + revision.toDouble()) * 100) / 100

    fun snapshot(): LenientIdlingSlot {
        return LenientIdlingSlot(
            code = code,
            label = label,
            archived = archived,
            minimum = minimum,
            notes = notes,
            version = version,
            dock = dock,
            revision = revision,
            distanceKm = distanceKm,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient idling slot in status open. */
fun newLenientIdlingSlot(code: String, label: String): LenientIdlingSlot {
    return LenientIdlingSlot(
        code = code,
        label = label,
        archived = false,
        minimum = 0.0,
        notes = "",
        version = 0,
        dock = "",
        revision = 0,
        distanceKm = 0.0,
        externalRef = "",
        status = LenientIdlingSlotStatus.OPEN,
    )
}
