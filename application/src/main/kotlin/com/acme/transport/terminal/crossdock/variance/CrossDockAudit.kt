package com.acme.transport.terminal.crossdock.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock audit is in its life cycle. */
enum class CrossDockAuditStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a cross dock audit and its current status. */
data class CrossDockAudit(
    var code: String,
    var name: String,
    var region: String,
    var trailerId: String,
    var locale: String,
    var checksum: String,
    var externalRef: String,
    var ceiling: Double,
    var depot: String,
    var priority: Int,
    var status: CrossDockAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockAuditStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockAuditStatus): Boolean {
        when (status) {
            CrossDockAuditStatus.CREATED -> {
                when (next) {
                    CrossDockAuditStatus.VALIDATED, CrossDockAuditStatus.RETIRED -> return true
                }
                return false
            }
            CrossDockAuditStatus.VALIDATED -> {
                when (next) {
                    CrossDockAuditStatus.PUBLISHED, CrossDockAuditStatus.RETIRED -> return true
                }
                return false
            }
            CrossDockAuditStatus.PUBLISHED -> {
                when (next) {
                    CrossDockAuditStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockAuditStatus) {
        if (!canMoveTo(next)) {
            error("cross dock audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round((ceiling + priority.toDouble()) * 100) / 100

    fun snapshot(): CrossDockAudit {
        return CrossDockAudit(
            code = code,
            name = name,
            region = region,
            trailerId = trailerId,
            locale = locale,
            checksum = checksum,
            externalRef = externalRef,
            ceiling = ceiling,
            depot = depot,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a cross dock audit in status created. */
fun newCrossDockAudit(code: String, name: String): CrossDockAudit {
    return CrossDockAudit(
        code = code,
        name = name,
        region = "",
        trailerId = "",
        locale = "",
        checksum = "",
        externalRef = "",
        ceiling = 0.0,
        depot = "",
        priority = 0,
        status = CrossDockAuditStatus.CREATED,
    )
}
