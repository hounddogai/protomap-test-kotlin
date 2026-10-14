package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial mapping is in its life cycle. */
enum class AccessorialMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A accessorial mapping as the accessorial team keeps it. */
data class AccessorialMapping(
    var code: String,
    var label: String,
    var version: Int,
    var batchSize: Int,
    var country: String,
    var status: AccessorialMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialMappingStatus): Boolean {
        when (status) {
            AccessorialMappingStatus.NEW -> return next == AccessorialMappingStatus.QUEUED || next == AccessorialMappingStatus.RUNNING
            AccessorialMappingStatus.QUEUED -> return next == AccessorialMappingStatus.FAILED
            AccessorialMappingStatus.RUNNING -> {
                when (next) {
                    AccessorialMappingStatus.DONE, AccessorialMappingStatus.FAILED -> return true
                }
                return false
            }
            AccessorialMappingStatus.DONE -> {
                when (next) {
                    AccessorialMappingStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialMappingStatus) {
        if (!canMoveTo(next)) {
            error("accessorial mapping $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun netLoad(): Double = round(version.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): AccessorialMapping {
        return AccessorialMapping(
            code = code,
            label = label,
            version = version,
            batchSize = batchSize,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a accessorial mapping in status new. */
fun newAccessorialMapping(code: String, label: String): AccessorialMapping {
    return AccessorialMapping(
        code = code,
        label = label,
        version = 0,
        batchSize = 0,
        country = "",
        status = AccessorialMappingStatus.NEW,
    )
}
