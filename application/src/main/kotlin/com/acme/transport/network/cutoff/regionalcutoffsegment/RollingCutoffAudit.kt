package com.acme.transport.network.cutoff.regionalcutoffsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling cutoff audit is in its life cycle. */
enum class RollingCutoffAuditStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a rolling cutoff audit and its current status. */
data class RollingCutoffAudit(
    var code: String,
    var title: String,
    var version: Int,
    var amount: Double,
    var revision: Int,
    var visible: Boolean,
    var source: String,
    var attributes: MutableMap<String, String>,
    var locale: String,
    var ceiling: Double,
    var status: RollingCutoffAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCutoffAuditStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCutoffAuditStatus): Boolean {
        when (status) {
            RollingCutoffAuditStatus.CREATED -> {
                when (next) {
                    RollingCutoffAuditStatus.VALIDATED, RollingCutoffAuditStatus.PUBLISHED -> return true
                }
                return false
            }
            RollingCutoffAuditStatus.VALIDATED -> return next == RollingCutoffAuditStatus.PUBLISHED || next == RollingCutoffAuditStatus.RETIRED
            RollingCutoffAuditStatus.PUBLISHED -> return next == RollingCutoffAuditStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingCutoffAuditStatus) {
        if (!canMoveTo(next)) {
            error("rolling cutoff audit $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netValue(): Double = round(amount * revision.toDouble() * 100) / 100

    fun snapshot(): RollingCutoffAudit {
        return RollingCutoffAudit(
            code = code,
            title = title,
            version = version,
            amount = amount,
            revision = revision,
            visible = visible,
            source = source,
            attributes = attributes,
            locale = locale,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a rolling cutoff audit in status created. */
fun newRollingCutoffAudit(code: String, title: String): RollingCutoffAudit {
    return RollingCutoffAudit(
        code = code,
        title = title,
        version = 0,
        amount = 0.0,
        revision = 0,
        visible = false,
        source = "",
        attributes = mutableMapOf<String, String>(),
        locale = "",
        ceiling = 0.0,
        status = RollingCutoffAuditStatus.CREATED,
    )
}
