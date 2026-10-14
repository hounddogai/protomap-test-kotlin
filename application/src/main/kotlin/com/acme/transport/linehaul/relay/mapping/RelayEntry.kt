package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.kit.text.normalize

/** Where a relay entry is in its life cycle. */
enum class RelayEntryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One relay entry, with what the planners need to act on it. */
data class RelayEntry(
    var code: String,
    var label: String,
    var contactEmail: String,
    var batchSize: Int,
    var vehicleId: String,
    var status: RelayEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayEntryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayEntryStatus): Boolean {
        when (status) {
            RelayEntryStatus.NEW -> return next == RelayEntryStatus.QUEUED || next == RelayEntryStatus.FAILED
            RelayEntryStatus.QUEUED -> return next == RelayEntryStatus.RUNNING || next == RelayEntryStatus.DONE
            RelayEntryStatus.RUNNING -> {
                when (next) {
                    RelayEntryStatus.DONE -> return true
                }
                return false
            }
            RelayEntryStatus.DONE -> {
                when (next) {
                    RelayEntryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayEntryStatus) {
        if (!canMoveTo(next)) {
            error("relay entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): RelayEntry {
        return RelayEntry(
            code = code,
            label = label,
            contactEmail = contactEmail,
            batchSize = batchSize,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a relay entry in status new. */
fun newRelayEntry(code: String, label: String): RelayEntry {
    return RelayEntry(
        code = code,
        label = label,
        contactEmail = "",
        batchSize = 0,
        vehicleId = "",
        status = RelayEntryStatus.NEW,
    )
}
