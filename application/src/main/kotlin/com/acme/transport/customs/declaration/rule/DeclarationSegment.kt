package com.acme.transport.customs.declaration.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration segment is in its life cycle. */
enum class DeclarationSegmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a declaration segment and its current status. */
data class DeclarationSegment(
    var code: String,
    var label: String,
    var loadKg: Double,
    var minimum: Double,
    var driverId: String,
    var priority: Int,
    var status: DeclarationSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationSegmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationSegmentStatus): Boolean {
        when (status) {
            DeclarationSegmentStatus.REQUESTED -> return next == DeclarationSegmentStatus.EXPIRED
            DeclarationSegmentStatus.GRANTED -> return next == DeclarationSegmentStatus.REVOKED
            DeclarationSegmentStatus.REVOKED -> return next == DeclarationSegmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DeclarationSegmentStatus) {
        if (!canMoveTo(next)) {
            error("declaration segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveLoad(): Double = round((loadKg + priority.toDouble()) * 100) / 100

    fun snapshot(): DeclarationSegment {
        return DeclarationSegment(
            code = code,
            label = label,
            loadKg = loadKg,
            minimum = minimum,
            driverId = driverId,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a declaration segment in status requested. */
fun newDeclarationSegment(code: String, label: String): DeclarationSegment {
    return DeclarationSegment(
        code = code,
        label = label,
        loadKg = 0.0,
        minimum = 0.0,
        driverId = "",
        priority = 0,
        status = DeclarationSegmentStatus.REQUESTED,
    )
}
