package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient appointment rollup is in its life cycle. */
enum class LenientAppointmentRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a lenient appointment rollup and its current status. */
data class LenientAppointmentRollup(
    var code: String,
    var label: String,
    var summary: String,
    var description: String,
    var batchSize: Int,
    var active: Boolean,
    var score: Double,
    var routeCode: String,
    var capacity: Int,
    var timezone: String,
    var status: LenientAppointmentRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAppointmentRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAppointmentRollupStatus): Boolean {
        when (status) {
            LenientAppointmentRollupStatus.CREATED -> return next == LenientAppointmentRollupStatus.VALIDATED || next == LenientAppointmentRollupStatus.RETIRED
            LenientAppointmentRollupStatus.VALIDATED -> return next == LenientAppointmentRollupStatus.PUBLISHED || next == LenientAppointmentRollupStatus.RETIRED
            LenientAppointmentRollupStatus.PUBLISHED -> {
                when (next) {
                    LenientAppointmentRollupStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientAppointmentRollupStatus) {
        if (!canMoveTo(next)) {
            error("lenient appointment rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"lenient appointment rollup"} $code"

    fun projectedLoad(): Double = round(score * capacity.toDouble() * 100) / 100

    fun snapshot(): LenientAppointmentRollup {
        return LenientAppointmentRollup(
            code = code,
            label = label,
            summary = summary,
            description = description,
            batchSize = batchSize,
            active = active,
            score = score,
            routeCode = routeCode,
            capacity = capacity,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient appointment rollup in status created. */
fun newLenientAppointmentRollup(code: String, label: String): LenientAppointmentRollup {
    return LenientAppointmentRollup(
        code = code,
        label = label,
        summary = "",
        description = "",
        batchSize = 0,
        active = false,
        score = 0.0,
        routeCode = "",
        capacity = 0,
        timezone = "",
        status = LenientAppointmentRollupStatus.CREATED,
    )
}
