package com.acme.transport.terminal.dockdoor.tiereddooragreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional dock door agreement records of terminal's dock door work, and the rules, stores, and
// calculations around them.

/** Where a regional dock door agreement is in its life cycle. */
enum class RegionalDockDoorAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a regional dock door agreement and its current status. */
data class RegionalDockDoorAgreement(
    var code: String,
    var title: String,
    var priority: Int,
    var tags: MutableList<String>,
    var archived: Boolean,
    var capacity: Int,
    var weight: Double,
    var lane: String,
    var odometer: Int,
    var budget: Double,
    var amount: Double,
    var status: RegionalDockDoorAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDockDoorAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDockDoorAgreementStatus): Boolean {
        when (status) {
            RegionalDockDoorAgreementStatus.NEW -> {
                when (next) {
                    RegionalDockDoorAgreementStatus.DONE, RegionalDockDoorAgreementStatus.FAILED -> return true
                }
                return false
            }
            RegionalDockDoorAgreementStatus.QUEUED -> return next == RegionalDockDoorAgreementStatus.DONE || next == RegionalDockDoorAgreementStatus.FAILED
            RegionalDockDoorAgreementStatus.RUNNING -> {
                when (next) {
                    RegionalDockDoorAgreementStatus.DONE, RegionalDockDoorAgreementStatus.FAILED -> return true
                }
                return false
            }
            RegionalDockDoorAgreementStatus.DONE -> {
                when (next) {
                    RegionalDockDoorAgreementStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDockDoorAgreementStatus) {
        if (!canMoveTo(next)) {
            error("regional dock door agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional dock door agreement"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedTotal(): Double = round((amount - budget) * 100) / 100

    fun snapshot(): RegionalDockDoorAgreement {
        return RegionalDockDoorAgreement(
            code = code,
            title = title,
            priority = priority,
            tags = tags,
            archived = archived,
            capacity = capacity,
            weight = weight,
            lane = lane,
            odometer = odometer,
            budget = budget,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a regional dock door agreement in status new. */
fun newRegionalDockDoorAgreement(code: String, title: String): RegionalDockDoorAgreement {
    return RegionalDockDoorAgreement(
        code = code,
        title = title,
        priority = 0,
        tags = mutableListOf<String>(),
        archived = false,
        capacity = 0,
        weight = 0.0,
        lane = "",
        odometer = 0,
        budget = 0.0,
        amount = 0.0,
        status = RegionalDockDoorAgreementStatus.NEW,
    )
}
