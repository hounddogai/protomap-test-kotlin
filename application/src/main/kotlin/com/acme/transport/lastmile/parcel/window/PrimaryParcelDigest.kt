package com.acme.transport.lastmile.parcel.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary parcel digest is in its life cycle. */
enum class PrimaryParcelDigestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a primary parcel digest and its current status. */
data class PrimaryParcelDigest(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var archived: Boolean,
    var quantity: Int,
    var currency: String,
    var updatedAt: Int,
    var dock: String,
    var status: PrimaryParcelDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryParcelDigestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryParcelDigestStatus): Boolean {
        when (status) {
            PrimaryParcelDigestStatus.CREATED -> return next == PrimaryParcelDigestStatus.PUBLISHED
            PrimaryParcelDigestStatus.VALIDATED -> return next == PrimaryParcelDigestStatus.PUBLISHED || next == PrimaryParcelDigestStatus.RETIRED
            PrimaryParcelDigestStatus.PUBLISHED -> {
                when (next) {
                    PrimaryParcelDigestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryParcelDigestStatus) {
        if (!canMoveTo(next)) {
            error("primary parcel digest $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"primary parcel digest"} $code"

    fun netValue(): Double = round((quantity.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): PrimaryParcelDigest {
        return PrimaryParcelDigest(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            archived = archived,
            quantity = quantity,
            currency = currency,
            updatedAt = updatedAt,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary parcel digest in status created. */
fun newPrimaryParcelDigest(code: String, name: String): PrimaryParcelDigest {
    return PrimaryParcelDigest(
        code = code,
        name = name,
        etaMinutes = 0,
        archived = false,
        quantity = 0,
        currency = "",
        updatedAt = 0,
        dock = "",
        status = PrimaryParcelDigestStatus.CREATED,
    )
}
