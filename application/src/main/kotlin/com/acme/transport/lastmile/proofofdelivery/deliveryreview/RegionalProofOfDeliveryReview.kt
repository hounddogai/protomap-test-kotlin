package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional proof of delivery review records of lastmile's proof of delivery work, and the rules, stores,
// and calculations around them.

/** Where a regional proof of delivery review is in its life cycle. */
enum class RegionalProofOfDeliveryReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One regional proof of delivery review, with what the planners need to act on it. */
data class RegionalProofOfDeliveryReview(
    var code: String,
    var name: String,
    var dock: String,
    var tags: MutableList<String>,
    var contactEmail: String,
    var batchSize: Int,
    var endsAt: Int,
    var status: RegionalProofOfDeliveryReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalProofOfDeliveryReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalProofOfDeliveryReviewStatus): Boolean {
        when (status) {
            RegionalProofOfDeliveryReviewStatus.NEW -> return next == RegionalProofOfDeliveryReviewStatus.DONE
            RegionalProofOfDeliveryReviewStatus.QUEUED -> {
                when (next) {
                    RegionalProofOfDeliveryReviewStatus.DONE -> return true
                }
                return false
            }
            RegionalProofOfDeliveryReviewStatus.RUNNING -> return next == RegionalProofOfDeliveryReviewStatus.FAILED
            RegionalProofOfDeliveryReviewStatus.DONE -> return next == RegionalProofOfDeliveryReviewStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalProofOfDeliveryReviewStatus) {
        if (!canMoveTo(next)) {
            error("regional proof of delivery review $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"regional proof of delivery review"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedValue(): Double = round((endsAt.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): RegionalProofOfDeliveryReview {
        return RegionalProofOfDeliveryReview(
            code = code,
            name = name,
            dock = dock,
            tags = tags,
            contactEmail = contactEmail,
            batchSize = batchSize,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional proof of delivery review in status new. */
fun newRegionalProofOfDeliveryReview(code: String, name: String): RegionalProofOfDeliveryReview {
    return RegionalProofOfDeliveryReview(
        code = code,
        name = name,
        dock = "",
        tags = mutableListOf<String>(),
        contactEmail = "",
        batchSize = 0,
        endsAt = 0,
        status = RegionalProofOfDeliveryReviewStatus.NEW,
    )
}
