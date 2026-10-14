package com.acme.transport.customs.inspection.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending inspection notice records of customs's inspection work, and the rules, stores, and calculations
// around them.

/** Where a pending inspection notice is in its life cycle. */
enum class PendingInspectionNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A pending inspection notice. Its code is unique within a tenant. */
data class PendingInspectionNotice(
    var code: String,
    var title: String,
    var loadKg: Double,
    var externalRef: String,
    var revision: Int,
    var rate: Double,
    var locale: String,
    var pallets: Int,
    var name: String,
    var expiresAt: Int,
    var status: PendingInspectionNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingInspectionNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingInspectionNoticeStatus): Boolean {
        when (status) {
            PendingInspectionNoticeStatus.NEW -> {
                when (next) {
                    PendingInspectionNoticeStatus.FAILED -> return true
                }
                return false
            }
            PendingInspectionNoticeStatus.QUEUED -> {
                when (next) {
                    PendingInspectionNoticeStatus.DONE -> return true
                }
                return false
            }
            PendingInspectionNoticeStatus.RUNNING -> {
                when (next) {
                    PendingInspectionNoticeStatus.DONE -> return true
                }
                return false
            }
            PendingInspectionNoticeStatus.DONE -> {
                when (next) {
                    PendingInspectionNoticeStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingInspectionNoticeStatus) {
        if (!canMoveTo(next)) {
            error("pending inspection notice $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun netTotal(): Double = round(pallets.toDouble() * rate * 100) / 100

    fun snapshot(): PendingInspectionNotice {
        return PendingInspectionNotice(
            code = code,
            title = title,
            loadKg = loadKg,
            externalRef = externalRef,
            revision = revision,
            rate = rate,
            locale = locale,
            pallets = pallets,
            name = name,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a pending inspection notice in status new. */
fun newPendingInspectionNotice(code: String, title: String): PendingInspectionNotice {
    return PendingInspectionNotice(
        code = code,
        title = title,
        loadKg = 0.0,
        externalRef = "",
        revision = 0,
        rate = 0.0,
        locale = "",
        pallets = 0,
        name = "",
        expiresAt = 0,
        status = PendingInspectionNoticeStatus.NEW,
    )
}
