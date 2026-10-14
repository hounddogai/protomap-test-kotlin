package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered broker score is in its life cycle. */
enum class TieredBrokerScoreStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a tiered broker score and its current status. */
data class TieredBrokerScore(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var label: String,
    var tags: MutableList<String>,
    var pallets: Int,
    var visible: Boolean,
    var margin: Double,
    var discount: Double,
    var status: TieredBrokerScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredBrokerScoreStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredBrokerScoreStatus): Boolean {
        when (status) {
            TieredBrokerScoreStatus.OPEN -> return next == TieredBrokerScoreStatus.RESOLVED || next == TieredBrokerScoreStatus.REOPENED
            TieredBrokerScoreStatus.IN_REVIEW -> return next == TieredBrokerScoreStatus.ARCHIVED
            TieredBrokerScoreStatus.RESOLVED -> return next == TieredBrokerScoreStatus.REOPENED
            TieredBrokerScoreStatus.REOPENED -> {
                when (next) {
                    TieredBrokerScoreStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredBrokerScoreStatus) {
        if (!canMoveTo(next)) {
            error("tiered broker score $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"tiered broker score"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedTotal(): Double = round((pallets.toDouble() - discount) * 100) / 100

    fun snapshot(): TieredBrokerScore {
        return TieredBrokerScore(
            code = code,
            title = title,
            expiresAt = expiresAt,
            label = label,
            tags = tags,
            pallets = pallets,
            visible = visible,
            margin = margin,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered broker score in status open. */
fun newTieredBrokerScore(code: String, title: String): TieredBrokerScore {
    return TieredBrokerScore(
        code = code,
        title = title,
        expiresAt = 0,
        label = "",
        tags = mutableListOf<String>(),
        pallets = 0,
        visible = false,
        margin = 0.0,
        discount = 0.0,
        status = TieredBrokerScoreStatus.OPEN,
    )
}
