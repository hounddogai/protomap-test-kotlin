package com.acme.transport.lastmile.parcel.parcelslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled parcel digest is in its life cycle. */
enum class ScheduledParcelDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled parcel digest. Its code is unique within a tenant. */
data class ScheduledParcelDigest(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var externalRef: String,
    var endsAt: Int,
    var status: ScheduledParcelDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledParcelDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledParcelDigestStatus): Boolean {
        when (status) {
            ScheduledParcelDigestStatus.NEW -> return next == ScheduledParcelDigestStatus.QUEUED || next == ScheduledParcelDigestStatus.RUNNING
            ScheduledParcelDigestStatus.QUEUED -> {
                when (next) {
                    ScheduledParcelDigestStatus.FAILED -> return true
                }
                return false
            }
            ScheduledParcelDigestStatus.RUNNING -> return next == ScheduledParcelDigestStatus.DONE || next == ScheduledParcelDigestStatus.FAILED
            ScheduledParcelDigestStatus.DONE -> return next == ScheduledParcelDigestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledParcelDigestStatus) {
        if (!canMoveTo(next)) {
            error("scheduled parcel digest $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveExposure(): Double = round((updatedAt.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledParcelDigest {
        return ScheduledParcelDigest(
            code = code,
            label = label,
            updatedAt = updatedAt,
            externalRef = externalRef,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled parcel digest in status new. */
fun newScheduledParcelDigest(code: String, label: String): ScheduledParcelDigest {
    return ScheduledParcelDigest(
        code = code,
        label = label,
        updatedAt = 0,
        externalRef = "",
        endsAt = 0,
        status = ScheduledParcelDigestStatus.NEW,
    )
}
