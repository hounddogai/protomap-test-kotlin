package com.acme.transport.customs.tariff.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff notice is in its life cycle. */
enum class TariffNoticeStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tariff notice. Its code is unique within a tenant. */
data class TariffNotice(
    var code: String,
    var title: String,
    var externalRef: String,
    var budget: Double,
    var carrier: String,
    var etaMinutes: Int,
    var segment: String,
    var updatedAt: Int,
    var active: Boolean,
    var sequence: Int,
    var status: TariffNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffNoticeStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffNoticeStatus): Boolean {
        when (status) {
            TariffNoticeStatus.OPEN -> {
                when (next) {
                    TariffNoticeStatus.REOPENED, TariffNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
            TariffNoticeStatus.IN_REVIEW -> {
                when (next) {
                    TariffNoticeStatus.REOPENED, TariffNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
            TariffNoticeStatus.RESOLVED -> {
                when (next) {
                    TariffNoticeStatus.REOPENED -> return true
                }
                return false
            }
            TariffNoticeStatus.REOPENED -> {
                when (next) {
                    TariffNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffNoticeStatus) {
        if (!canMoveTo(next)) {
            error("tariff notice $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tariff notice"} $code"

    fun weightedTotal(): Double = round((etaMinutes.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): TariffNotice {
        return TariffNotice(
            code = code,
            title = title,
            externalRef = externalRef,
            budget = budget,
            carrier = carrier,
            etaMinutes = etaMinutes,
            segment = segment,
            updatedAt = updatedAt,
            active = active,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tariff notice in status open. */
fun newTariffNotice(code: String, title: String): TariffNotice {
    return TariffNotice(
        code = code,
        title = title,
        externalRef = "",
        budget = 0.0,
        carrier = "",
        etaMinutes = 0,
        segment = "",
        updatedAt = 0,
        active = false,
        sequence = 0,
        status = TariffNoticeStatus.OPEN,
    )
}
