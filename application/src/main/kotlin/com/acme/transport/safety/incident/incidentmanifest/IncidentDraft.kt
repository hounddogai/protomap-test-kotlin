package com.acme.transport.safety.incident.incidentmanifest

import com.acme.transport.kit.text.normalize

/** Where a incident draft is in its life cycle. */
enum class IncidentDraftStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a incident draft and its current status. */
data class IncidentDraft(
    var code: String,
    var label: String,
    var name: String,
    var sequence: Int,
    var contactEmail: String,
    var channel: String,
    var externalRef: String,
    var unit: String,
    var status: IncidentDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentDraftStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentDraftStatus): Boolean {
        when (status) {
            IncidentDraftStatus.NEW -> return next == IncidentDraftStatus.FAILED
            IncidentDraftStatus.QUEUED -> return next == IncidentDraftStatus.RUNNING || next == IncidentDraftStatus.FAILED
            IncidentDraftStatus.RUNNING -> {
                when (next) {
                    IncidentDraftStatus.DONE, IncidentDraftStatus.FAILED -> return true
                }
                return false
            }
            IncidentDraftStatus.DONE -> return next == IncidentDraftStatus.FAILED
        }
        return false
    }

    fun moveTo(next: IncidentDraftStatus) {
        if (!canMoveTo(next)) {
            error("incident draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"incident draft"} $code"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): IncidentDraft {
        return IncidentDraft(
            code = code,
            label = label,
            name = name,
            sequence = sequence,
            contactEmail = contactEmail,
            channel = channel,
            externalRef = externalRef,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a incident draft in status new. */
fun newIncidentDraft(code: String, label: String): IncidentDraft {
    return IncidentDraft(
        code = code,
        label = label,
        name = "",
        sequence = 0,
        contactEmail = "",
        channel = "",
        externalRef = "",
        unit = "",
        status = IncidentDraftStatus.NEW,
    )
}
