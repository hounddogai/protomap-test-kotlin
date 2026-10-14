package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy inspection rule records of customs's inspection work, and the rules, stores, and calculations
// around them.

/** Where a legacy inspection rule is in its life cycle. */
enum class LegacyInspectionRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a legacy inspection rule and its current status. */
data class LegacyInspectionRule(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var etaMinutes: Int,
    var dock: String,
    var archived: Boolean,
    var driverId: String,
    var shiftCode: String,
    var tags: MutableList<String>,
    var version: Int,
    var status: LegacyInspectionRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyInspectionRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyInspectionRuleStatus): Boolean {
        when (status) {
            LegacyInspectionRuleStatus.REQUESTED -> {
                when (next) {
                    LegacyInspectionRuleStatus.REVOKED, LegacyInspectionRuleStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyInspectionRuleStatus.GRANTED -> {
                when (next) {
                    LegacyInspectionRuleStatus.REVOKED, LegacyInspectionRuleStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyInspectionRuleStatus.REVOKED -> {
                when (next) {
                    LegacyInspectionRuleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyInspectionRuleStatus) {
        if (!canMoveTo(next)) {
            error("legacy inspection rule $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"legacy inspection rule"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveTotal(): Double = round(version.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): LegacyInspectionRule {
        return LegacyInspectionRule(
            code = code,
            name = name,
            updatedAt = updatedAt,
            etaMinutes = etaMinutes,
            dock = dock,
            archived = archived,
            driverId = driverId,
            shiftCode = shiftCode,
            tags = tags,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy inspection rule in status requested. */
fun newLegacyInspectionRule(code: String, name: String): LegacyInspectionRule {
    return LegacyInspectionRule(
        code = code,
        name = name,
        updatedAt = 0,
        etaMinutes = 0,
        dock = "",
        archived = false,
        driverId = "",
        shiftCode = "",
        tags = mutableListOf<String>(),
        version = 0,
        status = LegacyInspectionRuleStatus.REQUESTED,
    )
}
