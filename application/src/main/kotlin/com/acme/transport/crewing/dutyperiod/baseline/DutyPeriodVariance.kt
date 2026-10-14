package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.kit.text.normalize

/** Where a duty period variance is in its life cycle. */
enum class DutyPeriodVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A duty period variance. Its code is unique within a tenant. */
data class DutyPeriodVariance(
    var code: String,
    var name: String,
    var version: Int,
    var tags: MutableList<String>,
    var attributes: MutableMap<String, String>,
    var status: DutyPeriodVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodVarianceStatus): Boolean {
        when (status) {
            DutyPeriodVarianceStatus.PROPOSED -> {
                when (next) {
                    DutyPeriodVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
            DutyPeriodVarianceStatus.APPROVED -> {
                when (next) {
                    DutyPeriodVarianceStatus.REJECTED, DutyPeriodVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
            DutyPeriodVarianceStatus.REJECTED -> {
                when (next) {
                    DutyPeriodVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodVarianceStatus) {
        if (!canMoveTo(next)) {
            error("duty period variance $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"duty period variance"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): DutyPeriodVariance {
        return DutyPeriodVariance(
            code = code,
            name = name,
            version = version,
            tags = tags,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a duty period variance in status proposed. */
fun newDutyPeriodVariance(code: String, name: String): DutyPeriodVariance {
    return DutyPeriodVariance(
        code = code,
        name = name,
        version = 0,
        tags = mutableListOf<String>(),
        attributes = mutableMapOf<String, String>(),
        status = DutyPeriodVarianceStatus.PROPOSED,
    )
}
