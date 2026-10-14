package com.acme.transport.crewing.restbreak.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break notice is in its life cycle. */
enum class RestBreakNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rest break notice as the rest break team keeps it. */
data class RestBreakNotice(
    var code: String,
    var name: String,
    var title: String,
    var phone: String,
    var approved: Boolean,
    var revision: Int,
    var label: String,
    var windowDays: Int,
    var etaMinutes: Int,
    var description: String,
    var status: RestBreakNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakNoticeStatus): Boolean {
        when (status) {
            RestBreakNoticeStatus.IDLE -> return next == RestBreakNoticeStatus.WARMING || next == RestBreakNoticeStatus.STOPPED
            RestBreakNoticeStatus.WARMING -> return next == RestBreakNoticeStatus.STOPPED
            RestBreakNoticeStatus.READY -> {
                when (next) {
                    RestBreakNoticeStatus.DRAINING, RestBreakNoticeStatus.STOPPED -> return true
                }
                return false
            }
            RestBreakNoticeStatus.DRAINING -> {
                when (next) {
                    RestBreakNoticeStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakNoticeStatus) {
        if (!canMoveTo(next)) {
            error("rest break notice $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code: $name"

    fun effectiveValue(): Double = round((etaMinutes.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): RestBreakNotice {
        return RestBreakNotice(
            code = code,
            name = name,
            title = title,
            phone = phone,
            approved = approved,
            revision = revision,
            label = label,
            windowDays = windowDays,
            etaMinutes = etaMinutes,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rest break notice in status idle. */
fun newRestBreakNotice(code: String, name: String): RestBreakNotice {
    return RestBreakNotice(
        code = code,
        name = name,
        title = "",
        phone = "",
        approved = false,
        revision = 0,
        label = "",
        windowDays = 0,
        etaMinutes = 0,
        description = "",
        status = RestBreakNoticeStatus.IDLE,
    )
}
