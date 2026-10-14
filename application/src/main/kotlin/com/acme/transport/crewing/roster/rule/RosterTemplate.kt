package com.acme.transport.crewing.roster.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster template is in its life cycle. */
enum class RosterTemplateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A roster template. Its code is unique within a tenant. */
data class RosterTemplate(
    var code: String,
    var name: String,
    var threshold: Double,
    var graceDays: Int,
    var attempts: Int,
    var source: String,
    var title: String,
    var ceiling: Double,
    var fuelLiters: Double,
    var quantity: Int,
    var checksum: String,
    var status: RosterTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterTemplateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterTemplateStatus): Boolean {
        when (status) {
            RosterTemplateStatus.NEW -> return next == RosterTemplateStatus.FAILED
            RosterTemplateStatus.QUEUED -> {
                when (next) {
                    RosterTemplateStatus.FAILED -> return true
                }
                return false
            }
            RosterTemplateStatus.RUNNING -> return next == RosterTemplateStatus.DONE || next == RosterTemplateStatus.FAILED
            RosterTemplateStatus.DONE -> return next == RosterTemplateStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RosterTemplateStatus) {
        if (!canMoveTo(next)) {
            error("roster template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedTotal(): Double = round(attempts.toDouble() * ceiling * 100) / 100

    fun snapshot(): RosterTemplate {
        return RosterTemplate(
            code = code,
            name = name,
            threshold = threshold,
            graceDays = graceDays,
            attempts = attempts,
            source = source,
            title = title,
            ceiling = ceiling,
            fuelLiters = fuelLiters,
            quantity = quantity,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a roster template in status new. */
fun newRosterTemplate(code: String, name: String): RosterTemplate {
    return RosterTemplate(
        code = code,
        name = name,
        threshold = 0.0,
        graceDays = 0,
        attempts = 0,
        source = "",
        title = "",
        ceiling = 0.0,
        fuelLiters = 0.0,
        quantity = 0,
        checksum = "",
        status = RosterTemplateStatus.NEW,
    )
}
