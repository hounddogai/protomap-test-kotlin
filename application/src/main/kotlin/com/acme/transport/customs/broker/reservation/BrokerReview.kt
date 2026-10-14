package com.acme.transport.customs.broker.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker review is in its life cycle. */
enum class BrokerReviewStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A broker review. Its code is unique within a tenant. */
data class BrokerReview(
    var code: String,
    var title: String,
    var ceiling: Double,
    var expiresAt: Int,
    var maximum: Double,
    var quantity: Int,
    var revision: Int,
    var floorValue: Double,
    var name: String,
    var pallets: Int,
    var status: BrokerReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerReviewStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerReviewStatus): Boolean {
        when (status) {
            BrokerReviewStatus.CREATED -> {
                when (next) {
                    BrokerReviewStatus.PUBLISHED, BrokerReviewStatus.RETIRED -> return true
                }
                return false
            }
            BrokerReviewStatus.VALIDATED -> {
                when (next) {
                    BrokerReviewStatus.PUBLISHED -> return true
                }
                return false
            }
            BrokerReviewStatus.PUBLISHED -> return next == BrokerReviewStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BrokerReviewStatus) {
        if (!canMoveTo(next)) {
            error("broker review $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun projectedExposure(): Double = round(expiresAt.toDouble() * floorValue * 100) / 100

    fun snapshot(): BrokerReview {
        return BrokerReview(
            code = code,
            title = title,
            ceiling = ceiling,
            expiresAt = expiresAt,
            maximum = maximum,
            quantity = quantity,
            revision = revision,
            floorValue = floorValue,
            name = name,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a broker review in status created. */
fun newBrokerReview(code: String, title: String): BrokerReview {
    return BrokerReview(
        code = code,
        title = title,
        ceiling = 0.0,
        expiresAt = 0,
        maximum = 0.0,
        quantity = 0,
        revision = 0,
        floorValue = 0.0,
        name = "",
        pallets = 0,
        status = BrokerReviewStatus.CREATED,
    )
}
