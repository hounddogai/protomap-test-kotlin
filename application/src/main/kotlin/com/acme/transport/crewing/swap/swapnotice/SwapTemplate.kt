package com.acme.transport.crewing.swap.swapnotice

import com.acme.transport.kit.text.normalize

/** Where a swap template is in its life cycle. */
enum class SwapTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A swap template as the swap team keeps it. */
data class SwapTemplate(
    var code: String,
    var title: String,
    var timezone: String,
    var depot: String,
    var reviewer: String,
    var score: Double,
    var status: SwapTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapTemplateStatus): Boolean {
        when (status) {
            SwapTemplateStatus.DRAFT -> {
                when (next) {
                    SwapTemplateStatus.SUSPENDED -> return true
                }
                return false
            }
            SwapTemplateStatus.PENDING -> return next == SwapTemplateStatus.ACTIVE || next == SwapTemplateStatus.CLOSED
            SwapTemplateStatus.ACTIVE -> {
                when (next) {
                    SwapTemplateStatus.SUSPENDED, SwapTemplateStatus.CLOSED -> return true
                }
                return false
            }
            SwapTemplateStatus.SUSPENDED -> {
                when (next) {
                    SwapTemplateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapTemplateStatus) {
        if (!canMoveTo(next)) {
            error("swap template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = score

    fun snapshot(): SwapTemplate {
        return SwapTemplate(
            code = code,
            title = title,
            timezone = timezone,
            depot = depot,
            reviewer = reviewer,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a swap template in status draft. */
fun newSwapTemplate(code: String, title: String): SwapTemplate {
    return SwapTemplate(
        code = code,
        title = title,
        timezone = "",
        depot = "",
        reviewer = "",
        score = 0.0,
        status = SwapTemplateStatus.DRAFT,
    )
}
