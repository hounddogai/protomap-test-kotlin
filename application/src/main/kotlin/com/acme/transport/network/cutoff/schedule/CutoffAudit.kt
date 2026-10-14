package com.acme.transport.network.cutoff.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff audit is in its life cycle. */
enum class CutoffAuditStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A cutoff audit. Its code is unique within a tenant. */
data class CutoffAudit(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var threshold: Double,
    var ratio: Double,
    var version: Int,
    var depot: String,
    var startsAt: Int,
    var rate: Double,
    var pinned: Boolean,
    var status: CutoffAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffAuditStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffAuditStatus): Boolean {
        when (status) {
            CutoffAuditStatus.NEW -> return next == CutoffAuditStatus.DONE || next == CutoffAuditStatus.FAILED
            CutoffAuditStatus.QUEUED -> {
                when (next) {
                    CutoffAuditStatus.RUNNING, CutoffAuditStatus.DONE -> return true
                }
                return false
            }
            CutoffAuditStatus.RUNNING -> return next == CutoffAuditStatus.DONE || next == CutoffAuditStatus.FAILED
            CutoffAuditStatus.DONE -> {
                when (next) {
                    CutoffAuditStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CutoffAuditStatus) {
        if (!canMoveTo(next)) {
            error("cutoff audit $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun weightedValue(): Double = round((threshold - version.toDouble()) * 100) / 100

    fun snapshot(): CutoffAudit {
        return CutoffAudit(
            code = code,
            title = title,
            distanceKm = distanceKm,
            threshold = threshold,
            ratio = ratio,
            version = version,
            depot = depot,
            startsAt = startsAt,
            rate = rate,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a cutoff audit in status new. */
fun newCutoffAudit(code: String, title: String): CutoffAudit {
    return CutoffAudit(
        code = code,
        title = title,
        distanceKm = 0.0,
        threshold = 0.0,
        ratio = 0.0,
        version = 0,
        depot = "",
        startsAt = 0,
        rate = 0.0,
        pinned = false,
        status = CutoffAuditStatus.NEW,
    )
}
