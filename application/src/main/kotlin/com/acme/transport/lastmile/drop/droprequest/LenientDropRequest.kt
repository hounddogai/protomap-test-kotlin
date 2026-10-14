package com.acme.transport.lastmile.drop.droprequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient drop request records of lastmile's drop work, and the rules, stores, and calculations around
// them.

/** Where a lenient drop request is in its life cycle. */
enum class LenientDropRequestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lenient drop request. Its code is unique within a tenant. */
data class LenientDropRequest(
    var code: String,
    var title: String,
    var endsAt: Int,
    var locale: String,
    var updatedAt: Int,
    var retries: Int,
    var depot: String,
    var visible: Boolean,
    var status: LenientDropRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDropRequestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDropRequestStatus): Boolean {
        when (status) {
            LenientDropRequestStatus.DRAFT -> return next == LenientDropRequestStatus.ACTIVE
            LenientDropRequestStatus.PENDING -> return next == LenientDropRequestStatus.SUSPENDED
            LenientDropRequestStatus.ACTIVE -> {
                when (next) {
                    LenientDropRequestStatus.SUSPENDED, LenientDropRequestStatus.CLOSED -> return true
                }
                return false
            }
            LenientDropRequestStatus.SUSPENDED -> {
                when (next) {
                    LenientDropRequestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientDropRequestStatus) {
        if (!canMoveTo(next)) {
            error("lenient drop request $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netVolume(): Double = round((updatedAt.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): LenientDropRequest {
        return LenientDropRequest(
            code = code,
            title = title,
            endsAt = endsAt,
            locale = locale,
            updatedAt = updatedAt,
            retries = retries,
            depot = depot,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a lenient drop request in status draft. */
fun newLenientDropRequest(code: String, title: String): LenientDropRequest {
    return LenientDropRequest(
        code = code,
        title = title,
        endsAt = 0,
        locale = "",
        updatedAt = 0,
        retries = 0,
        depot = "",
        visible = false,
        status = LenientDropRequestStatus.DRAFT,
    )
}
