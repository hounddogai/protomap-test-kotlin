package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.text.normalize

// The violation entry records of safety's violation work, and the rules, stores, and calculations around
// them.

/** Where a violation entry is in its life cycle. */
enum class ViolationEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A violation entry. Its code is unique within a tenant. */
data class ViolationEntry(
    var code: String,
    var name: String,
    var label: String,
    var checksum: String,
    var title: String,
    var quantity: Int,
    var status: ViolationEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationEntryStatus): Boolean {
        when (status) {
            ViolationEntryStatus.REQUESTED -> return next == ViolationEntryStatus.REVOKED
            ViolationEntryStatus.GRANTED -> return next == ViolationEntryStatus.REVOKED || next == ViolationEntryStatus.EXPIRED
            ViolationEntryStatus.REVOKED -> return next == ViolationEntryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ViolationEntryStatus) {
        if (!canMoveTo(next)) {
            error("violation entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"violation entry"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): ViolationEntry {
        return ViolationEntry(
            code = code,
            name = name,
            label = label,
            checksum = checksum,
            title = title,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a violation entry in status requested. */
fun newViolationEntry(code: String, name: String): ViolationEntry {
    return ViolationEntry(
        code = code,
        name = name,
        label = "",
        checksum = "",
        title = "",
        quantity = 0,
        status = ViolationEntryStatus.REQUESTED,
    )
}
