package com.acme.transport.network.servicearea.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area variance is in its life cycle. */
enum class ServiceAreaVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a service area variance and its current status. */
data class ServiceAreaVariance(
    var code: String,
    var name: String,
    var margin: Double,
    var externalRef: String,
    var stopCount: Int,
    var rate: Double,
    var pinned: Boolean,
    var amount: Double,
    var summary: String,
    var country: String,
    var status: ServiceAreaVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaVarianceStatus): Boolean {
        when (status) {
            ServiceAreaVarianceStatus.OPEN -> return next == ServiceAreaVarianceStatus.RESOLVED
            ServiceAreaVarianceStatus.IN_REVIEW -> return next == ServiceAreaVarianceStatus.RESOLVED || next == ServiceAreaVarianceStatus.ARCHIVED
            ServiceAreaVarianceStatus.RESOLVED -> return next == ServiceAreaVarianceStatus.REOPENED || next == ServiceAreaVarianceStatus.ARCHIVED
            ServiceAreaVarianceStatus.REOPENED -> return next == ServiceAreaVarianceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ServiceAreaVarianceStatus) {
        if (!canMoveTo(next)) {
            error("service area variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"service area variance"} $code"

    fun projectedExposure(): Double = round((amount - rate) * 100) / 100

    fun snapshot(): ServiceAreaVariance {
        return ServiceAreaVariance(
            code = code,
            name = name,
            margin = margin,
            externalRef = externalRef,
            stopCount = stopCount,
            rate = rate,
            pinned = pinned,
            amount = amount,
            summary = summary,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a service area variance in status open. */
fun newServiceAreaVariance(code: String, name: String): ServiceAreaVariance {
    return ServiceAreaVariance(
        code = code,
        name = name,
        margin = 0.0,
        externalRef = "",
        stopCount = 0,
        rate = 0.0,
        pinned = false,
        amount = 0.0,
        summary = "",
        country = "",
        status = ServiceAreaVarianceStatus.OPEN,
    )
}
