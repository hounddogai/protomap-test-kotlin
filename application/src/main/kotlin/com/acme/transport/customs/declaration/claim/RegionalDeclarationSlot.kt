package com.acme.transport.customs.declaration.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional declaration slot is in its life cycle. */
enum class RegionalDeclarationSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a regional declaration slot and its current status. */
data class RegionalDeclarationSlot(
    var code: String,
    var label: String,
    var pallets: Int,
    var priority: Int,
    var attributes: MutableMap<String, String>,
    var externalRef: String,
    var status: RegionalDeclarationSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDeclarationSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDeclarationSlotStatus): Boolean {
        when (status) {
            RegionalDeclarationSlotStatus.NEW -> {
                when (next) {
                    RegionalDeclarationSlotStatus.RUNNING -> return true
                }
                return false
            }
            RegionalDeclarationSlotStatus.QUEUED -> {
                when (next) {
                    RegionalDeclarationSlotStatus.DONE, RegionalDeclarationSlotStatus.FAILED -> return true
                }
                return false
            }
            RegionalDeclarationSlotStatus.RUNNING -> {
                when (next) {
                    RegionalDeclarationSlotStatus.DONE -> return true
                }
                return false
            }
            RegionalDeclarationSlotStatus.DONE -> {
                when (next) {
                    RegionalDeclarationSlotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDeclarationSlotStatus) {
        if (!canMoveTo(next)) {
            error("regional declaration slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedExposure(): Double = round((priority.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): RegionalDeclarationSlot {
        return RegionalDeclarationSlot(
            code = code,
            label = label,
            pallets = pallets,
            priority = priority,
            attributes = attributes,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional declaration slot in status new. */
fun newRegionalDeclarationSlot(code: String, label: String): RegionalDeclarationSlot {
    return RegionalDeclarationSlot(
        code = code,
        label = label,
        pallets = 0,
        priority = 0,
        attributes = mutableMapOf<String, String>(),
        externalRef = "",
        status = RegionalDeclarationSlotStatus.NEW,
    )
}
