package com.acme.transport.terminal.sortation.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary sortation bundle is in its life cycle. */
enum class PrimarySortationBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A primary sortation bundle. Its code is unique within a tenant. */
data class PrimarySortationBundle(
    var code: String,
    var label: String,
    var contactEmail: String,
    var quantity: Int,
    var shiftCode: String,
    var checksum: String,
    var minimum: Double,
    var limit: Int,
    var routeCode: String,
    var status: PrimarySortationBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimarySortationBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimarySortationBundleStatus): Boolean {
        when (status) {
            PrimarySortationBundleStatus.NEW -> {
                when (next) {
                    PrimarySortationBundleStatus.QUEUED, PrimarySortationBundleStatus.DONE -> return true
                }
                return false
            }
            PrimarySortationBundleStatus.QUEUED -> {
                when (next) {
                    PrimarySortationBundleStatus.RUNNING, PrimarySortationBundleStatus.DONE -> return true
                }
                return false
            }
            PrimarySortationBundleStatus.RUNNING -> {
                when (next) {
                    PrimarySortationBundleStatus.DONE, PrimarySortationBundleStatus.FAILED -> return true
                }
                return false
            }
            PrimarySortationBundleStatus.DONE -> {
                when (next) {
                    PrimarySortationBundleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimarySortationBundleStatus) {
        if (!canMoveTo(next)) {
            error("primary sortation bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary sortation bundle"} $code"

    fun netTotal(): Double = round((quantity.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): PrimarySortationBundle {
        return PrimarySortationBundle(
            code = code,
            label = label,
            contactEmail = contactEmail,
            quantity = quantity,
            shiftCode = shiftCode,
            checksum = checksum,
            minimum = minimum,
            limit = limit,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary sortation bundle in status new. */
fun newPrimarySortationBundle(code: String, label: String): PrimarySortationBundle {
    return PrimarySortationBundle(
        code = code,
        label = label,
        contactEmail = "",
        quantity = 0,
        shiftCode = "",
        checksum = "",
        minimum = 0.0,
        limit = 0,
        routeCode = "",
        status = PrimarySortationBundleStatus.NEW,
    )
}
