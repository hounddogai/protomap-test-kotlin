package com.acme.transport.network.cutoff.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff request is in its life cycle. */
enum class CutoffRequestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A cutoff request. Its code is unique within a tenant. */
data class CutoffRequest(
    var code: String,
    var title: String,
    var archived: Boolean,
    var rate: Double,
    var revision: Int,
    var limit: Int,
    var segment: String,
    var startsAt: Int,
    var externalRef: String,
    var active: Boolean,
    var status: CutoffRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffRequestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffRequestStatus): Boolean {
        when (status) {
            CutoffRequestStatus.OPEN -> return next == CutoffRequestStatus.RESOLVED || next == CutoffRequestStatus.ARCHIVED
            CutoffRequestStatus.IN_REVIEW -> {
                when (next) {
                    CutoffRequestStatus.REOPENED, CutoffRequestStatus.ARCHIVED -> return true
                }
                return false
            }
            CutoffRequestStatus.RESOLVED -> return next == CutoffRequestStatus.REOPENED
            CutoffRequestStatus.REOPENED -> {
                when (next) {
                    CutoffRequestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CutoffRequestStatus) {
        if (!canMoveTo(next)) {
            error("cutoff request $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun adjustedTotal(): Double = round(limit.toDouble() * rate * 100) / 100

    fun snapshot(): CutoffRequest {
        return CutoffRequest(
            code = code,
            title = title,
            archived = archived,
            rate = rate,
            revision = revision,
            limit = limit,
            segment = segment,
            startsAt = startsAt,
            externalRef = externalRef,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a cutoff request in status open. */
fun newCutoffRequest(code: String, title: String): CutoffRequest {
    return CutoffRequest(
        code = code,
        title = title,
        archived = false,
        rate = 0.0,
        revision = 0,
        limit = 0,
        segment = "",
        startsAt = 0,
        externalRef = "",
        active = false,
        status = CutoffRequestStatus.OPEN,
    )
}
