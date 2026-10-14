package com.acme.transport.fleetops.defect.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary defect bundle is in its life cycle. */
enum class PrimaryDefectBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One primary defect bundle, with what the planners need to act on it. */
data class PrimaryDefectBundle(
    var code: String,
    var title: String,
    var approved: Boolean,
    var routeCode: String,
    var active: Boolean,
    var contactEmail: String,
    var revision: Int,
    var budget: Double,
    var status: PrimaryDefectBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDefectBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDefectBundleStatus): Boolean {
        when (status) {
            PrimaryDefectBundleStatus.NEW -> {
                when (next) {
                    PrimaryDefectBundleStatus.DONE -> return true
                }
                return false
            }
            PrimaryDefectBundleStatus.QUEUED -> {
                when (next) {
                    PrimaryDefectBundleStatus.RUNNING, PrimaryDefectBundleStatus.FAILED -> return true
                }
                return false
            }
            PrimaryDefectBundleStatus.RUNNING -> return next == PrimaryDefectBundleStatus.DONE || next == PrimaryDefectBundleStatus.FAILED
            PrimaryDefectBundleStatus.DONE -> return next == PrimaryDefectBundleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PrimaryDefectBundleStatus) {
        if (!canMoveTo(next)) {
            error("primary defect bundle $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"primary defect bundle"} $code"

    fun netValue(): Double = round((revision.toDouble() - budget) * 100) / 100

    fun snapshot(): PrimaryDefectBundle {
        return PrimaryDefectBundle(
            code = code,
            title = title,
            approved = approved,
            routeCode = routeCode,
            active = active,
            contactEmail = contactEmail,
            revision = revision,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary defect bundle in status new. */
fun newPrimaryDefectBundle(code: String, title: String): PrimaryDefectBundle {
    return PrimaryDefectBundle(
        code = code,
        title = title,
        approved = false,
        routeCode = "",
        active = false,
        contactEmail = "",
        revision = 0,
        budget = 0.0,
        status = PrimaryDefectBundleStatus.NEW,
    )
}
