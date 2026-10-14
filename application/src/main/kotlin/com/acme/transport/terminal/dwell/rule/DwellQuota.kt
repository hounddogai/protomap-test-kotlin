package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell quota is in its life cycle. */
enum class DwellQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a dwell quota and its current status. */
data class DwellQuota(
    var code: String,
    var label: String,
    var windowDays: Int,
    var rate: Double,
    var country: String,
    var discount: Double,
    var margin: Double,
    var pinned: Boolean,
    var visible: Boolean,
    var revision: Int,
    var status: DwellQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellQuotaStatus): Boolean {
        when (status) {
            DwellQuotaStatus.IDLE -> {
                when (next) {
                    DwellQuotaStatus.READY -> return true
                }
                return false
            }
            DwellQuotaStatus.WARMING -> return next == DwellQuotaStatus.STOPPED
            DwellQuotaStatus.READY -> {
                when (next) {
                    DwellQuotaStatus.STOPPED -> return true
                }
                return false
            }
            DwellQuotaStatus.DRAINING -> {
                when (next) {
                    DwellQuotaStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellQuotaStatus) {
        if (!canMoveTo(next)) {
            error("dwell quota $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveValue(): Double = round((revision.toDouble() + discount) * 100) / 100

    fun snapshot(): DwellQuota {
        return DwellQuota(
            code = code,
            label = label,
            windowDays = windowDays,
            rate = rate,
            country = country,
            discount = discount,
            margin = margin,
            pinned = pinned,
            visible = visible,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a dwell quota in status idle. */
fun newDwellQuota(code: String, label: String): DwellQuota {
    return DwellQuota(
        code = code,
        label = label,
        windowDays = 0,
        rate = 0.0,
        country = "",
        discount = 0.0,
        margin = 0.0,
        pinned = false,
        visible = false,
        revision = 0,
        status = DwellQuotaStatus.IDLE,
    )
}
