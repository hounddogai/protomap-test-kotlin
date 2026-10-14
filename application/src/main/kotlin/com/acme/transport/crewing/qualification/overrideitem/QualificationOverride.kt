package com.acme.transport.crewing.qualification.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The qualification override records of crewing's qualification work, and the rules, stores, and calculations
// around them.

/** Where a qualification override is in its life cycle. */
enum class QualificationOverrideStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A qualification override as the qualification team keeps it. */
data class QualificationOverride(
    var code: String,
    var title: String,
    var phone: String,
    var depot: String,
    var cost: Double,
    var floorValue: Double,
    var active: Boolean,
    var trailerId: String,
    var status: QualificationOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationOverrideStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationOverrideStatus): Boolean {
        when (status) {
            QualificationOverrideStatus.CREATED -> {
                when (next) {
                    QualificationOverrideStatus.VALIDATED, QualificationOverrideStatus.RETIRED -> return true
                }
                return false
            }
            QualificationOverrideStatus.VALIDATED -> {
                when (next) {
                    QualificationOverrideStatus.PUBLISHED, QualificationOverrideStatus.RETIRED -> return true
                }
                return false
            }
            QualificationOverrideStatus.PUBLISHED -> {
                when (next) {
                    QualificationOverrideStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationOverrideStatus) {
        if (!canMoveTo(next)) {
            error("qualification override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedTotal(): Double = round(floorValue * cost * 100) / 100

    fun snapshot(): QualificationOverride {
        return QualificationOverride(
            code = code,
            title = title,
            phone = phone,
            depot = depot,
            cost = cost,
            floorValue = floorValue,
            active = active,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a qualification override in status created. */
fun newQualificationOverride(code: String, title: String): QualificationOverride {
    return QualificationOverride(
        code = code,
        title = title,
        phone = "",
        depot = "",
        cost = 0.0,
        floorValue = 0.0,
        active = false,
        trailerId = "",
        status = QualificationOverrideStatus.CREATED,
    )
}
