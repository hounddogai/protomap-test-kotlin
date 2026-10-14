package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.text.normalize

/** Where a sortation quota is in its life cycle. */
enum class SortationQuotaStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a sortation quota and its current status. */
data class SortationQuota(
    var code: String,
    var name: String,
    var externalRef: String,
    var ratio: Double,
    var category: String,
    var status: SortationQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationQuotaStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationQuotaStatus): Boolean {
        when (status) {
            SortationQuotaStatus.PLANNED -> {
                when (next) {
                    SortationQuotaStatus.IN_PROGRESS -> return true
                }
                return false
            }
            SortationQuotaStatus.SCHEDULED -> {
                when (next) {
                    SortationQuotaStatus.IN_PROGRESS, SortationQuotaStatus.COMPLETED -> return true
                }
                return false
            }
            SortationQuotaStatus.IN_PROGRESS -> {
                when (next) {
                    SortationQuotaStatus.COMPLETED, SortationQuotaStatus.CANCELLED -> return true
                }
                return false
            }
            SortationQuotaStatus.COMPLETED -> {
                when (next) {
                    SortationQuotaStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationQuotaStatus) {
        if (!canMoveTo(next)) {
            error("sortation quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = ratio

    fun snapshot(): SortationQuota {
        return SortationQuota(
            code = code,
            name = name,
            externalRef = externalRef,
            ratio = ratio,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a sortation quota in status planned. */
fun newSortationQuota(code: String, name: String): SortationQuota {
    return SortationQuota(
        code = code,
        name = name,
        externalRef = "",
        ratio = 0.0,
        category = "",
        status = SortationQuotaStatus.PLANNED,
    )
}
