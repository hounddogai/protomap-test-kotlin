package com.acme.transport.customs.declaration.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The declaration threshold records of customs's declaration work, and the rules, stores, and calculations
// around them.

/** Where a declaration threshold is in its life cycle. */
enum class DeclarationThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A declaration threshold. Its code is unique within a tenant. */
data class DeclarationThreshold(
    var code: String,
    var title: String,
    var externalRef: String,
    var windowDays: Int,
    var driverId: String,
    var quantity: Int,
    var shiftCode: String,
    var score: Double,
    var endsAt: Int,
    var routeCode: String,
    var revision: Int,
    var status: DeclarationThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationThresholdStatus): Boolean {
        when (status) {
            DeclarationThresholdStatus.NEW -> {
                when (next) {
                    DeclarationThresholdStatus.QUEUED, DeclarationThresholdStatus.DONE -> return true
                }
                return false
            }
            DeclarationThresholdStatus.QUEUED -> {
                when (next) {
                    DeclarationThresholdStatus.DONE, DeclarationThresholdStatus.FAILED -> return true
                }
                return false
            }
            DeclarationThresholdStatus.RUNNING -> {
                when (next) {
                    DeclarationThresholdStatus.DONE -> return true
                }
                return false
            }
            DeclarationThresholdStatus.DONE -> return next == DeclarationThresholdStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DeclarationThresholdStatus) {
        if (!canMoveTo(next)) {
            error("declaration threshold $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun projectedLoad(): Double = round((endsAt.toDouble() + score) * 100) / 100

    fun snapshot(): DeclarationThreshold {
        return DeclarationThreshold(
            code = code,
            title = title,
            externalRef = externalRef,
            windowDays = windowDays,
            driverId = driverId,
            quantity = quantity,
            shiftCode = shiftCode,
            score = score,
            endsAt = endsAt,
            routeCode = routeCode,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a declaration threshold in status new. */
fun newDeclarationThreshold(code: String, title: String): DeclarationThreshold {
    return DeclarationThreshold(
        code = code,
        title = title,
        externalRef = "",
        windowDays = 0,
        driverId = "",
        quantity = 0,
        shiftCode = "",
        score = 0.0,
        endsAt = 0,
        routeCode = "",
        revision = 0,
        status = DeclarationThresholdStatus.NEW,
    )
}
