package com.acme.transport.network.hub.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub review is in its life cycle. */
enum class HubReviewStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A hub review. Its code is unique within a tenant. */
data class HubReview(
    var code: String,
    var title: String,
    var routeCode: String,
    var phone: String,
    var pallets: Int,
    var rate: Double,
    var dock: String,
    var limit: Int,
    var approved: Boolean,
    var timezone: String,
    var createdAt: Int,
    var status: HubReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubReviewStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubReviewStatus): Boolean {
        when (status) {
            HubReviewStatus.DRAFT -> return next == HubReviewStatus.PENDING || next == HubReviewStatus.CLOSED
            HubReviewStatus.PENDING -> return next == HubReviewStatus.SUSPENDED || next == HubReviewStatus.CLOSED
            HubReviewStatus.ACTIVE -> return next == HubReviewStatus.SUSPENDED
            HubReviewStatus.SUSPENDED -> return next == HubReviewStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: HubReviewStatus) {
        if (!canMoveTo(next)) {
            error("hub review $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"hub review"} $code"

    fun projectedLoad(): Double = round((pallets.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): HubReview {
        return HubReview(
            code = code,
            title = title,
            routeCode = routeCode,
            phone = phone,
            pallets = pallets,
            rate = rate,
            dock = dock,
            limit = limit,
            approved = approved,
            timezone = timezone,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a hub review in status draft. */
fun newHubReview(code: String, title: String): HubReview {
    return HubReview(
        code = code,
        title = title,
        routeCode = "",
        phone = "",
        pallets = 0,
        rate = 0.0,
        dock = "",
        limit = 0,
        approved = false,
        timezone = "",
        createdAt = 0,
        status = HubReviewStatus.DRAFT,
    )
}
