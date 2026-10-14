package com.acme.transport.emissions.idling.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling snapshot is in its life cycle. */
enum class IdlingSnapshotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A idling snapshot. Its code is unique within a tenant. */
data class IdlingSnapshot(
    var code: String,
    var title: String,
    var segment: String,
    var createdAt: Int,
    var revision: Int,
    var region: String,
    var minimum: Double,
    var category: String,
    var retries: Int,
    var timezone: String,
    var status: IdlingSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingSnapshotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingSnapshotStatus): Boolean {
        when (status) {
            IdlingSnapshotStatus.REQUESTED -> {
                when (next) {
                    IdlingSnapshotStatus.REVOKED, IdlingSnapshotStatus.EXPIRED -> return true
                }
                return false
            }
            IdlingSnapshotStatus.GRANTED -> return next == IdlingSnapshotStatus.EXPIRED
            IdlingSnapshotStatus.REVOKED -> return next == IdlingSnapshotStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: IdlingSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("idling snapshot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"idling snapshot"} $code"

    fun netValue(): Double = round(retries.toDouble() * minimum * 100) / 100

    fun snapshot(): IdlingSnapshot {
        return IdlingSnapshot(
            code = code,
            title = title,
            segment = segment,
            createdAt = createdAt,
            revision = revision,
            region = region,
            minimum = minimum,
            category = category,
            retries = retries,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a idling snapshot in status requested. */
fun newIdlingSnapshot(code: String, title: String): IdlingSnapshot {
    return IdlingSnapshot(
        code = code,
        title = title,
        segment = "",
        createdAt = 0,
        revision = 0,
        region = "",
        minimum = 0.0,
        category = "",
        retries = 0,
        timezone = "",
        status = IdlingSnapshotStatus.REQUESTED,
    )
}
