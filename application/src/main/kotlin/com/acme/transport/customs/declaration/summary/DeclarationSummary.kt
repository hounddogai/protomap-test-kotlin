package com.acme.transport.customs.declaration.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The declaration summary records of customs's declaration work, and the rules, stores, and calculations
// around them.

/** Where a declaration summary is in its life cycle. */
enum class DeclarationSummaryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A declaration summary. Its code is unique within a tenant. */
data class DeclarationSummary(
    var code: String,
    var name: String,
    var stopCount: Int,
    var label: String,
    var priority: Int,
    var windowDays: Int,
    var vehicleId: String,
    var updatedAt: Int,
    var discount: Double,
    var margin: Double,
    var status: DeclarationSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationSummaryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationSummaryStatus): Boolean {
        when (status) {
            DeclarationSummaryStatus.OPEN -> return next == DeclarationSummaryStatus.IN_REVIEW || next == DeclarationSummaryStatus.RESOLVED
            DeclarationSummaryStatus.IN_REVIEW -> return next == DeclarationSummaryStatus.REOPENED || next == DeclarationSummaryStatus.ARCHIVED
            DeclarationSummaryStatus.RESOLVED -> {
                when (next) {
                    DeclarationSummaryStatus.REOPENED, DeclarationSummaryStatus.ARCHIVED -> return true
                }
                return false
            }
            DeclarationSummaryStatus.REOPENED -> {
                when (next) {
                    DeclarationSummaryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationSummaryStatus) {
        if (!canMoveTo(next)) {
            error("declaration summary $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($name)"

    fun effectiveTotal(): Double = round((discount + stopCount.toDouble()) * 100) / 100

    fun snapshot(): DeclarationSummary {
        return DeclarationSummary(
            code = code,
            name = name,
            stopCount = stopCount,
            label = label,
            priority = priority,
            windowDays = windowDays,
            vehicleId = vehicleId,
            updatedAt = updatedAt,
            discount = discount,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a declaration summary in status open. */
fun newDeclarationSummary(code: String, name: String): DeclarationSummary {
    return DeclarationSummary(
        code = code,
        name = name,
        stopCount = 0,
        label = "",
        priority = 0,
        windowDays = 0,
        vehicleId = "",
        updatedAt = 0,
        discount = 0.0,
        margin = 0.0,
        status = DeclarationSummaryStatus.OPEN,
    )
}
