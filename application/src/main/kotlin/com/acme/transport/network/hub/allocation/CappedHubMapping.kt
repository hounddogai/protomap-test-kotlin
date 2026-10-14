package com.acme.transport.network.hub.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped hub mapping is in its life cycle. */
enum class CappedHubMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capped hub mapping as the hub team keeps it. */
data class CappedHubMapping(
    var code: String,
    var name: String,
    var country: String,
    var batchSize: Int,
    var discount: Double,
    var ownerId: String,
    var retries: Int,
    var version: Int,
    var status: CappedHubMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedHubMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedHubMappingStatus): Boolean {
        when (status) {
            CappedHubMappingStatus.NEW -> return next == CappedHubMappingStatus.RUNNING || next == CappedHubMappingStatus.FAILED
            CappedHubMappingStatus.QUEUED -> return next == CappedHubMappingStatus.DONE
            CappedHubMappingStatus.RUNNING -> return next == CappedHubMappingStatus.DONE || next == CappedHubMappingStatus.FAILED
            CappedHubMappingStatus.DONE -> {
                when (next) {
                    CappedHubMappingStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedHubMappingStatus) {
        if (!canMoveTo(next)) {
            error("capped hub mapping $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun netTotal(): Double = round(version.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): CappedHubMapping {
        return CappedHubMapping(
            code = code,
            name = name,
            country = country,
            batchSize = batchSize,
            discount = discount,
            ownerId = ownerId,
            retries = retries,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped hub mapping in status new. */
fun newCappedHubMapping(code: String, name: String): CappedHubMapping {
    return CappedHubMapping(
        code = code,
        name = name,
        country = "",
        batchSize = 0,
        discount = 0.0,
        ownerId = "",
        retries = 0,
        version = 0,
        status = CappedHubMappingStatus.NEW,
    )
}
