package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global trailer yard audit records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a global trailer yard audit is in its life cycle. */
enum class GlobalTrailerYardAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a global trailer yard audit and its current status. */
data class GlobalTrailerYardAudit(
    var code: String,
    var name: String,
    var threshold: Double,
    var lane: String,
    var description: String,
    var loadKg: Double,
    var fuelLiters: Double,
    var archived: Boolean,
    var region: String,
    var status: GlobalTrailerYardAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTrailerYardAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTrailerYardAuditStatus): Boolean {
        when (status) {
            GlobalTrailerYardAuditStatus.PLANNED -> {
                when (next) {
                    GlobalTrailerYardAuditStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalTrailerYardAuditStatus.SCHEDULED -> {
                when (next) {
                    GlobalTrailerYardAuditStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalTrailerYardAuditStatus.IN_PROGRESS -> {
                when (next) {
                    GlobalTrailerYardAuditStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalTrailerYardAuditStatus.COMPLETED -> return next == GlobalTrailerYardAuditStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: GlobalTrailerYardAuditStatus) {
        if (!canMoveTo(next)) {
            error("global trailer yard audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global trailer yard audit"} $code"

    fun netExposure(): Double = round((fuelLiters + loadKg) * 100) / 100

    fun snapshot(): GlobalTrailerYardAudit {
        return GlobalTrailerYardAudit(
            code = code,
            name = name,
            threshold = threshold,
            lane = lane,
            description = description,
            loadKg = loadKg,
            fuelLiters = fuelLiters,
            archived = archived,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a global trailer yard audit in status planned. */
fun newGlobalTrailerYardAudit(code: String, name: String): GlobalTrailerYardAudit {
    return GlobalTrailerYardAudit(
        code = code,
        name = name,
        threshold = 0.0,
        lane = "",
        description = "",
        loadKg = 0.0,
        fuelLiters = 0.0,
        archived = false,
        region = "",
        status = GlobalTrailerYardAuditStatus.PLANNED,
    )
}
