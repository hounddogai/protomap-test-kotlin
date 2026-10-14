package com.acme.transport.lastmile.redelivery.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary redelivery preference is in its life cycle. */
enum class PrimaryRedeliveryPreferenceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One primary redelivery preference, with what the planners need to act on it. */
data class PrimaryRedeliveryPreference(
    var code: String,
    var name: String,
    var endsAt: Int,
    var visible: Boolean,
    var retries: Int,
    var checksum: String,
    var phone: String,
    var pallets: Int,
    var archived: Boolean,
    var status: PrimaryRedeliveryPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRedeliveryPreferenceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRedeliveryPreferenceStatus): Boolean {
        when (status) {
            PrimaryRedeliveryPreferenceStatus.NEW -> {
                when (next) {
                    PrimaryRedeliveryPreferenceStatus.DONE, PrimaryRedeliveryPreferenceStatus.FAILED -> return true
                }
                return false
            }
            PrimaryRedeliveryPreferenceStatus.QUEUED -> {
                when (next) {
                    PrimaryRedeliveryPreferenceStatus.RUNNING, PrimaryRedeliveryPreferenceStatus.DONE -> return true
                }
                return false
            }
            PrimaryRedeliveryPreferenceStatus.RUNNING -> {
                when (next) {
                    PrimaryRedeliveryPreferenceStatus.FAILED -> return true
                }
                return false
            }
            PrimaryRedeliveryPreferenceStatus.DONE -> return next == PrimaryRedeliveryPreferenceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PrimaryRedeliveryPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("primary redelivery preference $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun netLoad(): Double = round((endsAt.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): PrimaryRedeliveryPreference {
        return PrimaryRedeliveryPreference(
            code = code,
            name = name,
            endsAt = endsAt,
            visible = visible,
            retries = retries,
            checksum = checksum,
            phone = phone,
            pallets = pallets,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary redelivery preference in status new. */
fun newPrimaryRedeliveryPreference(code: String, name: String): PrimaryRedeliveryPreference {
    return PrimaryRedeliveryPreference(
        code = code,
        name = name,
        endsAt = 0,
        visible = false,
        retries = 0,
        checksum = "",
        phone = "",
        pallets = 0,
        archived = false,
        status = PrimaryRedeliveryPreferenceStatus.NEW,
    )
}
