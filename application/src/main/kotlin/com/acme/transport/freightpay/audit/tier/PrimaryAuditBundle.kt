package com.acme.transport.freightpay.audit.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary audit bundle is in its life cycle. */
enum class PrimaryAuditBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One primary audit bundle, with what the planners need to act on it. */
data class PrimaryAuditBundle(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var stopCount: Int,
    var summary: String,
    var createdAt: Int,
    var loadKg: Double,
    var contactEmail: String,
    var startsAt: Int,
    var status: PrimaryAuditBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryAuditBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryAuditBundleStatus): Boolean {
        when (status) {
            PrimaryAuditBundleStatus.NEW -> {
                when (next) {
                    PrimaryAuditBundleStatus.RUNNING, PrimaryAuditBundleStatus.FAILED -> return true
                }
                return false
            }
            PrimaryAuditBundleStatus.QUEUED -> {
                when (next) {
                    PrimaryAuditBundleStatus.FAILED -> return true
                }
                return false
            }
            PrimaryAuditBundleStatus.RUNNING -> return next == PrimaryAuditBundleStatus.DONE || next == PrimaryAuditBundleStatus.FAILED
            PrimaryAuditBundleStatus.DONE -> {
                when (next) {
                    PrimaryAuditBundleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryAuditBundleStatus) {
        if (!canMoveTo(next)) {
            error("primary audit bundle $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedExposure(): Double = round(stopCount.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): PrimaryAuditBundle {
        return PrimaryAuditBundle(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            stopCount = stopCount,
            summary = summary,
            createdAt = createdAt,
            loadKg = loadKg,
            contactEmail = contactEmail,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary audit bundle in status new. */
fun newPrimaryAuditBundle(code: String, title: String): PrimaryAuditBundle {
    return PrimaryAuditBundle(
        code = code,
        title = title,
        etaMinutes = 0,
        stopCount = 0,
        summary = "",
        createdAt = 0,
        loadKg = 0.0,
        contactEmail = "",
        startsAt = 0,
        status = PrimaryAuditBundleStatus.NEW,
    )
}
