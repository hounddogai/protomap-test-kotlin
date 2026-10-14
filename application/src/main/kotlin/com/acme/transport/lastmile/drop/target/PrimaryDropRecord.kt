package com.acme.transport.lastmile.drop.target

import com.acme.transport.kit.text.normalize

/** Where a primary drop record is in its life cycle. */
enum class PrimaryDropRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a primary drop record and its current status. */
data class PrimaryDropRecord(
    var code: String,
    var name: String,
    var checksum: String,
    var approved: Boolean,
    var timezone: String,
    var windowDays: Int,
    var vehicleId: String,
    var visible: Boolean,
    var archived: Boolean,
    var lane: String,
    var attributes: MutableMap<String, String>,
    var status: PrimaryDropRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDropRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDropRecordStatus): Boolean {
        when (status) {
            PrimaryDropRecordStatus.PROPOSED -> return next == PrimaryDropRecordStatus.APPROVED
            PrimaryDropRecordStatus.APPROVED -> {
                when (next) {
                    PrimaryDropRecordStatus.REJECTED, PrimaryDropRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryDropRecordStatus.REJECTED -> return next == PrimaryDropRecordStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PrimaryDropRecordStatus) {
        if (!canMoveTo(next)) {
            error("primary drop record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary drop record"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): PrimaryDropRecord {
        return PrimaryDropRecord(
            code = code,
            name = name,
            checksum = checksum,
            approved = approved,
            timezone = timezone,
            windowDays = windowDays,
            vehicleId = vehicleId,
            visible = visible,
            archived = archived,
            lane = lane,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a primary drop record in status proposed. */
fun newPrimaryDropRecord(code: String, name: String): PrimaryDropRecord {
    return PrimaryDropRecord(
        code = code,
        name = name,
        checksum = "",
        approved = false,
        timezone = "",
        windowDays = 0,
        vehicleId = "",
        visible = false,
        archived = false,
        lane = "",
        attributes = mutableMapOf<String, String>(),
        status = PrimaryDropRecordStatus.PROPOSED,
    )
}
