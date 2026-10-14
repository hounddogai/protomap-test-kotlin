package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area review is in its life cycle. */
enum class ServiceAreaReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a service area review and its current status. */
data class ServiceAreaReview(
    var code: String,
    var title: String,
    var externalRef: String,
    var batchSize: Int,
    var ratio: Double,
    var status: ServiceAreaReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaReviewStatus): Boolean {
        when (status) {
            ServiceAreaReviewStatus.NEW -> {
                when (next) {
                    ServiceAreaReviewStatus.QUEUED -> return true
                }
                return false
            }
            ServiceAreaReviewStatus.QUEUED -> {
                when (next) {
                    ServiceAreaReviewStatus.RUNNING, ServiceAreaReviewStatus.DONE -> return true
                }
                return false
            }
            ServiceAreaReviewStatus.RUNNING -> return next == ServiceAreaReviewStatus.FAILED
            ServiceAreaReviewStatus.DONE -> {
                when (next) {
                    ServiceAreaReviewStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaReviewStatus) {
        if (!canMoveTo(next)) {
            error("service area review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"service area review"} $code"

    fun effectiveExposure(): Double = round((ratio - batchSize.toDouble()) * 100) / 100

    fun snapshot(): ServiceAreaReview {
        return ServiceAreaReview(
            code = code,
            title = title,
            externalRef = externalRef,
            batchSize = batchSize,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a service area review in status new. */
fun newServiceAreaReview(code: String, title: String): ServiceAreaReview {
    return ServiceAreaReview(
        code = code,
        title = title,
        externalRef = "",
        batchSize = 0,
        ratio = 0.0,
        status = ServiceAreaReviewStatus.NEW,
    )
}
