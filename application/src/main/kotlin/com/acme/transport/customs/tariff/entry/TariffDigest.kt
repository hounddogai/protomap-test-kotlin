package com.acme.transport.customs.tariff.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff digest is in its life cycle. */
enum class TariffDigestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A tariff digest. Its code is unique within a tenant. */
data class TariffDigest(
    var code: String,
    var title: String,
    var active: Boolean,
    var pallets: Int,
    var archived: Boolean,
    var reviewer: String,
    var ratio: Double,
    var rate: Double,
    var revision: Int,
    var status: TariffDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffDigestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffDigestStatus): Boolean {
        when (status) {
            TariffDigestStatus.DRAFT -> {
                when (next) {
                    TariffDigestStatus.ACTIVE, TariffDigestStatus.SUSPENDED -> return true
                }
                return false
            }
            TariffDigestStatus.PENDING -> return next == TariffDigestStatus.ACTIVE || next == TariffDigestStatus.CLOSED
            TariffDigestStatus.ACTIVE -> return next == TariffDigestStatus.CLOSED
            TariffDigestStatus.SUSPENDED -> {
                when (next) {
                    TariffDigestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffDigestStatus) {
        if (!canMoveTo(next)) {
            error("tariff digest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun adjustedExposure(): Double = round((rate - ratio) * 100) / 100

    fun snapshot(): TariffDigest {
        return TariffDigest(
            code = code,
            title = title,
            active = active,
            pallets = pallets,
            archived = archived,
            reviewer = reviewer,
            ratio = ratio,
            rate = rate,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tariff digest in status draft. */
fun newTariffDigest(code: String, title: String): TariffDigest {
    return TariffDigest(
        code = code,
        title = title,
        active = false,
        pallets = 0,
        archived = false,
        reviewer = "",
        ratio = 0.0,
        rate = 0.0,
        revision = 0,
        status = TariffDigestStatus.DRAFT,
    )
}
