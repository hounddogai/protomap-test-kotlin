package com.acme.transport.customs.broker.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default broker draft is in its life cycle. */
enum class DefaultBrokerDraftStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a default broker draft and its current status. */
data class DefaultBrokerDraft(
    var code: String,
    var label: String,
    var ownerId: String,
    var windowDays: Int,
    var odometer: Int,
    var driverId: String,
    var sequence: Int,
    var score: Double,
    var archived: Boolean,
    var status: DefaultBrokerDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultBrokerDraftStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultBrokerDraftStatus): Boolean {
        when (status) {
            DefaultBrokerDraftStatus.CREATED -> {
                when (next) {
                    DefaultBrokerDraftStatus.PUBLISHED, DefaultBrokerDraftStatus.RETIRED -> return true
                }
                return false
            }
            DefaultBrokerDraftStatus.VALIDATED -> return next == DefaultBrokerDraftStatus.PUBLISHED || next == DefaultBrokerDraftStatus.RETIRED
            DefaultBrokerDraftStatus.PUBLISHED -> {
                when (next) {
                    DefaultBrokerDraftStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultBrokerDraftStatus) {
        if (!canMoveTo(next)) {
            error("default broker draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedVolume(): Double = round((sequence.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): DefaultBrokerDraft {
        return DefaultBrokerDraft(
            code = code,
            label = label,
            ownerId = ownerId,
            windowDays = windowDays,
            odometer = odometer,
            driverId = driverId,
            sequence = sequence,
            score = score,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default broker draft in status created. */
fun newDefaultBrokerDraft(code: String, label: String): DefaultBrokerDraft {
    return DefaultBrokerDraft(
        code = code,
        label = label,
        ownerId = "",
        windowDays = 0,
        odometer = 0,
        driverId = "",
        sequence = 0,
        score = 0.0,
        archived = false,
        status = DefaultBrokerDraftStatus.CREATED,
    )
}
