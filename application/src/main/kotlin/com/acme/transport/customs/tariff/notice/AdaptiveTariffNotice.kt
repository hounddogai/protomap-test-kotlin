package com.acme.transport.customs.tariff.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive tariff notice records of customs's tariff work, and the rules, stores, and calculations around
// them.

/** Where a adaptive tariff notice is in its life cycle. */
enum class AdaptiveTariffNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One adaptive tariff notice, with what the planners need to act on it. */
data class AdaptiveTariffNotice(
    var code: String,
    var name: String,
    var threshold: Double,
    var endsAt: Int,
    var rate: Double,
    var cost: Double,
    var revision: Int,
    var retries: Int,
    var status: AdaptiveTariffNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTariffNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTariffNoticeStatus): Boolean {
        when (status) {
            AdaptiveTariffNoticeStatus.NEW -> {
                when (next) {
                    AdaptiveTariffNoticeStatus.QUEUED, AdaptiveTariffNoticeStatus.DONE -> return true
                }
                return false
            }
            AdaptiveTariffNoticeStatus.QUEUED -> return next == AdaptiveTariffNoticeStatus.DONE
            AdaptiveTariffNoticeStatus.RUNNING -> {
                when (next) {
                    AdaptiveTariffNoticeStatus.DONE, AdaptiveTariffNoticeStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveTariffNoticeStatus.DONE -> {
                when (next) {
                    AdaptiveTariffNoticeStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveTariffNoticeStatus) {
        if (!canMoveTo(next)) {
            error("adaptive tariff notice $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun netExposure(): Double = round((threshold + endsAt.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveTariffNotice {
        return AdaptiveTariffNotice(
            code = code,
            name = name,
            threshold = threshold,
            endsAt = endsAt,
            rate = rate,
            cost = cost,
            revision = revision,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive tariff notice in status new. */
fun newAdaptiveTariffNotice(code: String, name: String): AdaptiveTariffNotice {
    return AdaptiveTariffNotice(
        code = code,
        name = name,
        threshold = 0.0,
        endsAt = 0,
        rate = 0.0,
        cost = 0.0,
        revision = 0,
        retries = 0,
        status = AdaptiveTariffNoticeStatus.NEW,
    )
}
