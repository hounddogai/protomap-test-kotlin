package com.acme.transport.customs.declaration.declarationmanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling declaration group is in its life cycle. */
enum class RollingDeclarationGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A rolling declaration group. Its code is unique within a tenant. */
data class RollingDeclarationGroup(
    var code: String,
    var title: String,
    var category: String,
    var windowDays: Int,
    var tags: MutableList<String>,
    var summary: String,
    var cost: Double,
    var maximum: Double,
    var retries: Int,
    var timezone: String,
    var batchSize: Int,
    var status: RollingDeclarationGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDeclarationGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDeclarationGroupStatus): Boolean {
        when (status) {
            RollingDeclarationGroupStatus.PLANNED -> {
                when (next) {
                    RollingDeclarationGroupStatus.COMPLETED -> return true
                }
                return false
            }
            RollingDeclarationGroupStatus.SCHEDULED -> return next == RollingDeclarationGroupStatus.COMPLETED
            RollingDeclarationGroupStatus.IN_PROGRESS -> {
                when (next) {
                    RollingDeclarationGroupStatus.COMPLETED -> return true
                }
                return false
            }
            RollingDeclarationGroupStatus.COMPLETED -> {
                when (next) {
                    RollingDeclarationGroupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingDeclarationGroupStatus) {
        if (!canMoveTo(next)) {
            error("rolling declaration group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round(windowDays.toDouble() * maximum * 100) / 100

    fun snapshot(): RollingDeclarationGroup {
        return RollingDeclarationGroup(
            code = code,
            title = title,
            category = category,
            windowDays = windowDays,
            tags = tags,
            summary = summary,
            cost = cost,
            maximum = maximum,
            retries = retries,
            timezone = timezone,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rolling declaration group in status planned. */
fun newRollingDeclarationGroup(code: String, title: String): RollingDeclarationGroup {
    return RollingDeclarationGroup(
        code = code,
        title = title,
        category = "",
        windowDays = 0,
        tags = mutableListOf<String>(),
        summary = "",
        cost = 0.0,
        maximum = 0.0,
        retries = 0,
        timezone = "",
        batchSize = 0,
        status = RollingDeclarationGroupStatus.PLANNED,
    )
}
