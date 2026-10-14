package com.acme.transport.customs.declaration.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default declaration record is in its life cycle. */
enum class DefaultDeclarationRecordStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A default declaration record. Its code is unique within a tenant. */
data class DefaultDeclarationRecord(
    var code: String,
    var label: String,
    var odometer: Int,
    var minimum: Double,
    var shiftCode: String,
    var trailerId: String,
    var startsAt: Int,
    var status: DefaultDeclarationRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDeclarationRecordStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDeclarationRecordStatus): Boolean {
        when (status) {
            DefaultDeclarationRecordStatus.OPEN -> return next == DefaultDeclarationRecordStatus.IN_REVIEW
            DefaultDeclarationRecordStatus.IN_REVIEW -> {
                when (next) {
                    DefaultDeclarationRecordStatus.REOPENED, DefaultDeclarationRecordStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultDeclarationRecordStatus.RESOLVED -> return next == DefaultDeclarationRecordStatus.REOPENED || next == DefaultDeclarationRecordStatus.ARCHIVED
            DefaultDeclarationRecordStatus.REOPENED -> {
                when (next) {
                    DefaultDeclarationRecordStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultDeclarationRecordStatus) {
        if (!canMoveTo(next)) {
            error("default declaration record $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"default declaration record"} $code"

    fun projectedValue(): Double = round((minimum - startsAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultDeclarationRecord {
        return DefaultDeclarationRecord(
            code = code,
            label = label,
            odometer = odometer,
            minimum = minimum,
            shiftCode = shiftCode,
            trailerId = trailerId,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default declaration record in status open. */
fun newDefaultDeclarationRecord(code: String, label: String): DefaultDeclarationRecord {
    return DefaultDeclarationRecord(
        code = code,
        label = label,
        odometer = 0,
        minimum = 0.0,
        shiftCode = "",
        trailerId = "",
        startsAt = 0,
        status = DefaultDeclarationRecordStatus.OPEN,
    )
}
