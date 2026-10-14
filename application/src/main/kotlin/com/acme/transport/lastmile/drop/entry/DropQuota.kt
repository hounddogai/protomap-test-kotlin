package com.acme.transport.lastmile.drop.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop quota is in its life cycle. */
enum class DropQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One drop quota, with what the planners need to act on it. */
data class DropQuota(
    var code: String,
    var title: String,
    var ratio: Double,
    var expiresAt: Int,
    var attributes: MutableMap<String, String>,
    var visible: Boolean,
    var trailerId: String,
    var notes: String,
    var ownerId: String,
    var status: DropQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropQuotaStatus): Boolean {
        when (status) {
            DropQuotaStatus.DRAFT -> {
                when (next) {
                    DropQuotaStatus.SUSPENDED -> return true
                }
                return false
            }
            DropQuotaStatus.PENDING -> return next == DropQuotaStatus.CLOSED
            DropQuotaStatus.ACTIVE -> return next == DropQuotaStatus.SUSPENDED || next == DropQuotaStatus.CLOSED
            DropQuotaStatus.SUSPENDED -> {
                when (next) {
                    DropQuotaStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropQuotaStatus) {
        if (!canMoveTo(next)) {
            error("drop quota $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedValue(): Double = round(ratio * expiresAt.toDouble() * 100) / 100

    fun snapshot(): DropQuota {
        return DropQuota(
            code = code,
            title = title,
            ratio = ratio,
            expiresAt = expiresAt,
            attributes = attributes,
            visible = visible,
            trailerId = trailerId,
            notes = notes,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a drop quota in status draft. */
fun newDropQuota(code: String, title: String): DropQuota {
    return DropQuota(
        code = code,
        title = title,
        ratio = 0.0,
        expiresAt = 0,
        attributes = mutableMapOf<String, String>(),
        visible = false,
        trailerId = "",
        notes = "",
        ownerId = "",
        status = DropQuotaStatus.DRAFT,
    )
}
