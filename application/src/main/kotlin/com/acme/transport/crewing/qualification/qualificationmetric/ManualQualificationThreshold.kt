package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual qualification threshold is in its life cycle. */
enum class ManualQualificationThresholdStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a manual qualification threshold and its current status. */
data class ManualQualificationThreshold(
    var code: String,
    var name: String,
    var createdAt: Int,
    var expiresAt: Int,
    var lane: String,
    var reviewer: String,
    var carrier: String,
    var region: String,
    var attributes: MutableMap<String, String>,
    var status: ManualQualificationThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualQualificationThresholdStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualQualificationThresholdStatus): Boolean {
        when (status) {
            ManualQualificationThresholdStatus.IDLE -> return next == ManualQualificationThresholdStatus.READY || next == ManualQualificationThresholdStatus.STOPPED
            ManualQualificationThresholdStatus.WARMING -> return next == ManualQualificationThresholdStatus.STOPPED
            ManualQualificationThresholdStatus.READY -> {
                when (next) {
                    ManualQualificationThresholdStatus.DRAINING -> return true
                }
                return false
            }
            ManualQualificationThresholdStatus.DRAINING -> return next == ManualQualificationThresholdStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualQualificationThresholdStatus) {
        if (!canMoveTo(next)) {
            error("manual qualification threshold $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netTotal(): Double = round(expiresAt.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): ManualQualificationThreshold {
        return ManualQualificationThreshold(
            code = code,
            name = name,
            createdAt = createdAt,
            expiresAt = expiresAt,
            lane = lane,
            reviewer = reviewer,
            carrier = carrier,
            region = region,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual qualification threshold in status idle. */
fun newManualQualificationThreshold(code: String, name: String): ManualQualificationThreshold {
    return ManualQualificationThreshold(
        code = code,
        name = name,
        createdAt = 0,
        expiresAt = 0,
        lane = "",
        reviewer = "",
        carrier = "",
        region = "",
        attributes = mutableMapOf<String, String>(),
        status = ManualQualificationThresholdStatus.IDLE,
    )
}
