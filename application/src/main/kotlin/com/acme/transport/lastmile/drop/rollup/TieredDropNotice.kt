package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered drop notice is in its life cycle. */
enum class TieredDropNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered drop notice as the drop team keeps it. */
data class TieredDropNotice(
    var code: String,
    var title: String,
    var locale: String,
    var externalRef: String,
    var fuelLiters: Double,
    var score: Double,
    var status: TieredDropNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDropNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDropNoticeStatus): Boolean {
        when (status) {
            TieredDropNoticeStatus.PROPOSED -> {
                when (next) {
                    TieredDropNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredDropNoticeStatus.APPROVED -> {
                when (next) {
                    TieredDropNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredDropNoticeStatus.REJECTED -> return next == TieredDropNoticeStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TieredDropNoticeStatus) {
        if (!canMoveTo(next)) {
            error("tiered drop notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedLoad(): Double = round((fuelLiters + score) * 100) / 100

    fun snapshot(): TieredDropNotice {
        return TieredDropNotice(
            code = code,
            title = title,
            locale = locale,
            externalRef = externalRef,
            fuelLiters = fuelLiters,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tiered drop notice in status proposed. */
fun newTieredDropNotice(code: String, title: String): TieredDropNotice {
    return TieredDropNotice(
        code = code,
        title = title,
        locale = "",
        externalRef = "",
        fuelLiters = 0.0,
        score = 0.0,
        status = TieredDropNoticeStatus.PROPOSED,
    )
}
