package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient node mapping is in its life cycle. */
enum class LenientNodeMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One lenient node mapping, with what the planners need to act on it. */
data class LenientNodeMapping(
    var code: String,
    var title: String,
    var retries: Int,
    var odometer: Int,
    var pallets: Int,
    var status: LenientNodeMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientNodeMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientNodeMappingStatus): Boolean {
        when (status) {
            LenientNodeMappingStatus.CREATED -> {
                when (next) {
                    LenientNodeMappingStatus.VALIDATED -> return true
                }
                return false
            }
            LenientNodeMappingStatus.VALIDATED -> return next == LenientNodeMappingStatus.RETIRED
            LenientNodeMappingStatus.PUBLISHED -> {
                when (next) {
                    LenientNodeMappingStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientNodeMappingStatus) {
        if (!canMoveTo(next)) {
            error("lenient node mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient node mapping"} $code"

    fun adjustedValue(): Double = round((retries.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): LenientNodeMapping {
        return LenientNodeMapping(
            code = code,
            title = title,
            retries = retries,
            odometer = odometer,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient node mapping in status created. */
fun newLenientNodeMapping(code: String, title: String): LenientNodeMapping {
    return LenientNodeMapping(
        code = code,
        title = title,
        retries = 0,
        odometer = 0,
        pallets = 0,
        status = LenientNodeMappingStatus.CREATED,
    )
}
