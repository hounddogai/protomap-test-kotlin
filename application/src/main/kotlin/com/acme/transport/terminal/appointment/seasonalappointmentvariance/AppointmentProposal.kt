package com.acme.transport.terminal.appointment.seasonalappointmentvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment proposal is in its life cycle. */
enum class AppointmentProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A appointment proposal. Its code is unique within a tenant. */
data class AppointmentProposal(
    var code: String,
    var label: String,
    var stopCount: Int,
    var etaMinutes: Int,
    var checksum: String,
    var status: AppointmentProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentProposalStatus): Boolean {
        when (status) {
            AppointmentProposalStatus.CREATED -> return next == AppointmentProposalStatus.RETIRED
            AppointmentProposalStatus.VALIDATED -> return next == AppointmentProposalStatus.RETIRED
            AppointmentProposalStatus.PUBLISHED -> {
                when (next) {
                    AppointmentProposalStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentProposalStatus) {
        if (!canMoveTo(next)) {
            error("appointment proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedExposure(): Double = round((stopCount.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): AppointmentProposal {
        return AppointmentProposal(
            code = code,
            label = label,
            stopCount = stopCount,
            etaMinutes = etaMinutes,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a appointment proposal in status created. */
fun newAppointmentProposal(code: String, label: String): AppointmentProposal {
    return AppointmentProposal(
        code = code,
        label = label,
        stopCount = 0,
        etaMinutes = 0,
        checksum = "",
        status = AppointmentProposalStatus.CREATED,
    )
}
