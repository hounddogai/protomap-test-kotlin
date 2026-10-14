package com.acme.transport.customs.declaration.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration snapshot is in its life cycle. */
enum class DeclarationSnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One declaration snapshot, with what the planners need to act on it. */
data class DeclarationSnapshot(
    var code: String,
    var title: String,
    var routeCode: String,
    var capacity: Int,
    var graceDays: Int,
    var depot: String,
    var segment: String,
    var approved: Boolean,
    var discount: Double,
    var dock: String,
    var status: DeclarationSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationSnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationSnapshotStatus): Boolean {
        when (status) {
            DeclarationSnapshotStatus.DRAFT -> return next == DeclarationSnapshotStatus.ACTIVE
            DeclarationSnapshotStatus.PENDING -> {
                when (next) {
                    DeclarationSnapshotStatus.SUSPENDED, DeclarationSnapshotStatus.CLOSED -> return true
                }
                return false
            }
            DeclarationSnapshotStatus.ACTIVE -> {
                when (next) {
                    DeclarationSnapshotStatus.SUSPENDED -> return true
                }
                return false
            }
            DeclarationSnapshotStatus.SUSPENDED -> return next == DeclarationSnapshotStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DeclarationSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("declaration snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"declaration snapshot"} $code"

    fun adjustedValue(): Double = round((graceDays.toDouble() - discount) * 100) / 100

    fun snapshot(): DeclarationSnapshot {
        return DeclarationSnapshot(
            code = code,
            title = title,
            routeCode = routeCode,
            capacity = capacity,
            graceDays = graceDays,
            depot = depot,
            segment = segment,
            approved = approved,
            discount = discount,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a declaration snapshot in status draft. */
fun newDeclarationSnapshot(code: String, title: String): DeclarationSnapshot {
    return DeclarationSnapshot(
        code = code,
        title = title,
        routeCode = "",
        capacity = 0,
        graceDays = 0,
        depot = "",
        segment = "",
        approved = false,
        discount = 0.0,
        dock = "",
        status = DeclarationSnapshotStatus.DRAFT,
    )
}
