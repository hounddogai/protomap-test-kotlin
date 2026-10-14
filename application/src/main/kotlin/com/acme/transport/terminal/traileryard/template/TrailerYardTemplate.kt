package com.acme.transport.terminal.traileryard.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer yard template records of terminal's trailer yard work, and the rules, stores, and calculations
// around them.

/** Where a trailer yard template is in its life cycle. */
enum class TrailerYardTemplateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A trailer yard template as the trailer yard team keeps it. */
data class TrailerYardTemplate(
    var code: String,
    var title: String,
    var attempts: Int,
    var lane: String,
    var discount: Double,
    var amount: Double,
    var status: TrailerYardTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardTemplateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardTemplateStatus): Boolean {
        when (status) {
            TrailerYardTemplateStatus.NEW -> return next == TrailerYardTemplateStatus.DONE || next == TrailerYardTemplateStatus.FAILED
            TrailerYardTemplateStatus.QUEUED -> {
                when (next) {
                    TrailerYardTemplateStatus.RUNNING, TrailerYardTemplateStatus.FAILED -> return true
                }
                return false
            }
            TrailerYardTemplateStatus.RUNNING -> {
                when (next) {
                    TrailerYardTemplateStatus.DONE, TrailerYardTemplateStatus.FAILED -> return true
                }
                return false
            }
            TrailerYardTemplateStatus.DONE -> {
                when (next) {
                    TrailerYardTemplateStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerYardTemplateStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveLoad(): Double = round((amount + discount) * 100) / 100

    fun snapshot(): TrailerYardTemplate {
        return TrailerYardTemplate(
            code = code,
            title = title,
            attempts = attempts,
            lane = lane,
            discount = discount,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a trailer yard template in status new. */
fun newTrailerYardTemplate(code: String, title: String): TrailerYardTemplate {
    return TrailerYardTemplate(
        code = code,
        title = title,
        attempts = 0,
        lane = "",
        discount = 0.0,
        amount = 0.0,
        status = TrailerYardTemplateStatus.NEW,
    )
}
