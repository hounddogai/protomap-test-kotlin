package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary trailer bundle is in its life cycle. */
enum class PrimaryTrailerBundleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a primary trailer bundle and its current status. */
data class PrimaryTrailerBundle(
    var code: String,
    var title: String,
    var locale: String,
    var floorValue: Double,
    var cost: Double,
    var unit: String,
    var quantity: Int,
    var minimum: Double,
    var expiresAt: Int,
    var status: PrimaryTrailerBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTrailerBundleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTrailerBundleStatus): Boolean {
        when (status) {
            PrimaryTrailerBundleStatus.DRAFT -> return next == PrimaryTrailerBundleStatus.ACTIVE || next == PrimaryTrailerBundleStatus.SUSPENDED
            PrimaryTrailerBundleStatus.PENDING -> return next == PrimaryTrailerBundleStatus.ACTIVE
            PrimaryTrailerBundleStatus.ACTIVE -> {
                when (next) {
                    PrimaryTrailerBundleStatus.SUSPENDED, PrimaryTrailerBundleStatus.CLOSED -> return true
                }
                return false
            }
            PrimaryTrailerBundleStatus.SUSPENDED -> return next == PrimaryTrailerBundleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PrimaryTrailerBundleStatus) {
        if (!canMoveTo(next)) {
            error("primary trailer bundle $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netVolume(): Double = round((cost - floorValue) * 100) / 100

    fun snapshot(): PrimaryTrailerBundle {
        return PrimaryTrailerBundle(
            code = code,
            title = title,
            locale = locale,
            floorValue = floorValue,
            cost = cost,
            unit = unit,
            quantity = quantity,
            minimum = minimum,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a primary trailer bundle in status draft. */
fun newPrimaryTrailerBundle(code: String, title: String): PrimaryTrailerBundle {
    return PrimaryTrailerBundle(
        code = code,
        title = title,
        locale = "",
        floorValue = 0.0,
        cost = 0.0,
        unit = "",
        quantity = 0,
        minimum = 0.0,
        expiresAt = 0,
        status = PrimaryTrailerBundleStatus.DRAFT,
    )
}
