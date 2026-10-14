package com.acme.transport.customs.declaration.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration window is in its life cycle. */
enum class DeclarationWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A declaration window as the declaration team keeps it. */
data class DeclarationWindow(
    var code: String,
    var name: String,
    var weight: Double,
    var checksum: String,
    var label: String,
    var routeCode: String,
    var fuelLiters: Double,
    var phone: String,
    var status: DeclarationWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationWindowStatus): Boolean {
        when (status) {
            DeclarationWindowStatus.NEW -> {
                when (next) {
                    DeclarationWindowStatus.QUEUED, DeclarationWindowStatus.DONE -> return true
                }
                return false
            }
            DeclarationWindowStatus.QUEUED -> return next == DeclarationWindowStatus.DONE || next == DeclarationWindowStatus.FAILED
            DeclarationWindowStatus.RUNNING -> return next == DeclarationWindowStatus.DONE || next == DeclarationWindowStatus.FAILED
            DeclarationWindowStatus.DONE -> return next == DeclarationWindowStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DeclarationWindowStatus) {
        if (!canMoveTo(next)) {
            error("declaration window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"declaration window"} $code"

    fun weightedLoad(): Double = round(fuelLiters * weight * 100) / 100

    fun snapshot(): DeclarationWindow {
        return DeclarationWindow(
            code = code,
            name = name,
            weight = weight,
            checksum = checksum,
            label = label,
            routeCode = routeCode,
            fuelLiters = fuelLiters,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a declaration window in status new. */
fun newDeclarationWindow(code: String, name: String): DeclarationWindow {
    return DeclarationWindow(
        code = code,
        name = name,
        weight = 0.0,
        checksum = "",
        label = "",
        routeCode = "",
        fuelLiters = 0.0,
        phone = "",
        status = DeclarationWindowStatus.NEW,
    )
}
