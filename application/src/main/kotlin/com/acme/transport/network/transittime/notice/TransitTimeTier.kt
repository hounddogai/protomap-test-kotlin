package com.acme.transport.network.transittime.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time tier is in its life cycle. */
enum class TransitTimeTierStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A transit time tier. Its code is unique within a tenant. */
data class TransitTimeTier(
    var code: String,
    var title: String,
    var score: Double,
    var pallets: Int,
    var startsAt: Int,
    var revision: Int,
    var status: TransitTimeTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeTierStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeTierStatus): Boolean {
        when (status) {
            TransitTimeTierStatus.CREATED -> {
                when (next) {
                    TransitTimeTierStatus.VALIDATED, TransitTimeTierStatus.PUBLISHED -> return true
                }
                return false
            }
            TransitTimeTierStatus.VALIDATED -> {
                when (next) {
                    TransitTimeTierStatus.PUBLISHED, TransitTimeTierStatus.RETIRED -> return true
                }
                return false
            }
            TransitTimeTierStatus.PUBLISHED -> return next == TransitTimeTierStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TransitTimeTierStatus) {
        if (!canMoveTo(next)) {
            error("transit time tier $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun adjustedVolume(): Double = round((revision.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): TransitTimeTier {
        return TransitTimeTier(
            code = code,
            title = title,
            score = score,
            pallets = pallets,
            startsAt = startsAt,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a transit time tier in status created. */
fun newTransitTimeTier(code: String, title: String): TransitTimeTier {
    return TransitTimeTier(
        code = code,
        title = title,
        score = 0.0,
        pallets = 0,
        startsAt = 0,
        revision = 0,
        status = TransitTimeTierStatus.CREATED,
    )
}
