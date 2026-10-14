package com.acme.transport.customs.declaration.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive declaration exception is in its life cycle. */
enum class AdaptiveDeclarationExceptionStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive declaration exception. Its code is unique within a tenant. */
data class AdaptiveDeclarationException(
    var code: String,
    var label: String,
    var dock: String,
    var odometer: Int,
    var ownerId: String,
    var stopCount: Int,
    var expiresAt: Int,
    var status: AdaptiveDeclarationExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDeclarationExceptionStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDeclarationExceptionStatus): Boolean {
        when (status) {
            AdaptiveDeclarationExceptionStatus.DRAFT -> return next == AdaptiveDeclarationExceptionStatus.PENDING || next == AdaptiveDeclarationExceptionStatus.ACTIVE
            AdaptiveDeclarationExceptionStatus.PENDING -> return next == AdaptiveDeclarationExceptionStatus.SUSPENDED || next == AdaptiveDeclarationExceptionStatus.CLOSED
            AdaptiveDeclarationExceptionStatus.ACTIVE -> {
                when (next) {
                    AdaptiveDeclarationExceptionStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveDeclarationExceptionStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveDeclarationExceptionStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveDeclarationExceptionStatus) {
        if (!canMoveTo(next)) {
            error("adaptive declaration exception $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedLoad(): Double = round((expiresAt.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveDeclarationException {
        return AdaptiveDeclarationException(
            code = code,
            label = label,
            dock = dock,
            odometer = odometer,
            ownerId = ownerId,
            stopCount = stopCount,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive declaration exception in status draft. */
fun newAdaptiveDeclarationException(code: String, label: String): AdaptiveDeclarationException {
    return AdaptiveDeclarationException(
        code = code,
        label = label,
        dock = "",
        odometer = 0,
        ownerId = "",
        stopCount = 0,
        expiresAt = 0,
        status = AdaptiveDeclarationExceptionStatus.DRAFT,
    )
}
