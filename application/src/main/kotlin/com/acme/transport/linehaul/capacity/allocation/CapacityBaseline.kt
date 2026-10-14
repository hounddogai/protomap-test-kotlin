package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity baseline is in its life cycle. */
enum class CapacityBaselineStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a capacity baseline and its current status. */
data class CapacityBaseline(
    var code: String,
    var title: String,
    var quantity: Int,
    var tags: MutableList<String>,
    var createdAt: Int,
    var dock: String,
    var expiresAt: Int,
    var region: String,
    var cost: Double,
    var score: Double,
    var status: CapacityBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityBaselineStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityBaselineStatus): Boolean {
        when (status) {
            CapacityBaselineStatus.CREATED -> {
                when (next) {
                    CapacityBaselineStatus.RETIRED -> return true
                }
                return false
            }
            CapacityBaselineStatus.VALIDATED -> {
                when (next) {
                    CapacityBaselineStatus.PUBLISHED, CapacityBaselineStatus.RETIRED -> return true
                }
                return false
            }
            CapacityBaselineStatus.PUBLISHED -> {
                when (next) {
                    CapacityBaselineStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityBaselineStatus) {
        if (!canMoveTo(next)) {
            error("capacity baseline $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"capacity baseline"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveValue(): Double = round((expiresAt.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): CapacityBaseline {
        return CapacityBaseline(
            code = code,
            title = title,
            quantity = quantity,
            tags = tags,
            createdAt = createdAt,
            dock = dock,
            expiresAt = expiresAt,
            region = region,
            cost = cost,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a capacity baseline in status created. */
fun newCapacityBaseline(code: String, title: String): CapacityBaseline {
    return CapacityBaseline(
        code = code,
        title = title,
        quantity = 0,
        tags = mutableListOf<String>(),
        createdAt = 0,
        dock = "",
        expiresAt = 0,
        region = "",
        cost = 0.0,
        score = 0.0,
        status = CapacityBaselineStatus.CREATED,
    )
}
