package com.acme.transport.linehaul.trailer.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped trailer preference is in its life cycle. */
enum class CappedTrailerPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped trailer preference as the trailer team keeps it. */
data class CappedTrailerPreference(
    var code: String,
    var title: String,
    var visible: Boolean,
    var ceiling: Double,
    var cost: Double,
    var priority: Int,
    var checksum: String,
    var name: String,
    var region: String,
    var loadKg: Double,
    var status: CappedTrailerPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTrailerPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTrailerPreferenceStatus): Boolean {
        when (status) {
            CappedTrailerPreferenceStatus.REQUESTED -> {
                when (next) {
                    CappedTrailerPreferenceStatus.GRANTED, CappedTrailerPreferenceStatus.REVOKED -> return true
                }
                return false
            }
            CappedTrailerPreferenceStatus.GRANTED -> {
                when (next) {
                    CappedTrailerPreferenceStatus.REVOKED, CappedTrailerPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
            CappedTrailerPreferenceStatus.REVOKED -> {
                when (next) {
                    CappedTrailerPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedTrailerPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("capped trailer preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedVolume(): Double = round(loadKg * ceiling * 100) / 100

    fun snapshot(): CappedTrailerPreference {
        return CappedTrailerPreference(
            code = code,
            title = title,
            visible = visible,
            ceiling = ceiling,
            cost = cost,
            priority = priority,
            checksum = checksum,
            name = name,
            region = region,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a capped trailer preference in status requested. */
fun newCappedTrailerPreference(code: String, title: String): CappedTrailerPreference {
    return CappedTrailerPreference(
        code = code,
        title = title,
        visible = false,
        ceiling = 0.0,
        cost = 0.0,
        priority = 0,
        checksum = "",
        name = "",
        region = "",
        loadKg = 0.0,
        status = CappedTrailerPreferenceStatus.REQUESTED,
    )
}
