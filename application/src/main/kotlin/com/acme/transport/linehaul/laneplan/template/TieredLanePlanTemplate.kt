package com.acme.transport.linehaul.laneplan.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered lane plan template records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a tiered lane plan template is in its life cycle. */
enum class TieredLanePlanTemplateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a tiered lane plan template and its current status. */
data class TieredLanePlanTemplate(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var archived: Boolean,
    var sequence: Int,
    var reviewer: String,
    var title: String,
    var vehicleId: String,
    var attempts: Int,
    var amount: Double,
    var checksum: String,
    var status: TieredLanePlanTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredLanePlanTemplateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredLanePlanTemplateStatus): Boolean {
        when (status) {
            TieredLanePlanTemplateStatus.NEW -> return next == TieredLanePlanTemplateStatus.QUEUED || next == TieredLanePlanTemplateStatus.FAILED
            TieredLanePlanTemplateStatus.QUEUED -> return next == TieredLanePlanTemplateStatus.RUNNING || next == TieredLanePlanTemplateStatus.DONE
            TieredLanePlanTemplateStatus.RUNNING -> return next == TieredLanePlanTemplateStatus.DONE || next == TieredLanePlanTemplateStatus.FAILED
            TieredLanePlanTemplateStatus.DONE -> return next == TieredLanePlanTemplateStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TieredLanePlanTemplateStatus) {
        if (!canMoveTo(next)) {
            error("tiered lane plan template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedVolume(): Double = round(attempts.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): TieredLanePlanTemplate {
        return TieredLanePlanTemplate(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            archived = archived,
            sequence = sequence,
            reviewer = reviewer,
            title = title,
            vehicleId = vehicleId,
            attempts = attempts,
            amount = amount,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered lane plan template in status new. */
fun newTieredLanePlanTemplate(code: String, name: String): TieredLanePlanTemplate {
    return TieredLanePlanTemplate(
        code = code,
        name = name,
        fuelLiters = 0.0,
        archived = false,
        sequence = 0,
        reviewer = "",
        title = "",
        vehicleId = "",
        attempts = 0,
        amount = 0.0,
        checksum = "",
        status = TieredLanePlanTemplateStatus.NEW,
    )
}
