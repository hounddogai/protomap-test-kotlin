package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.kit.text.normalize

/** Where a scheduled proof of delivery preference is in its life cycle. */
enum class ScheduledProofOfDeliveryPreferenceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled proof of delivery preference. Its code is unique within a tenant. */
data class ScheduledProofOfDeliveryPreference(
    var code: String,
    var name: String,
    var title: String,
    var checksum: String,
    var description: String,
    var batchSize: Int,
    var segment: String,
    var label: String,
    var status: ScheduledProofOfDeliveryPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledProofOfDeliveryPreferenceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledProofOfDeliveryPreferenceStatus): Boolean {
        when (status) {
            ScheduledProofOfDeliveryPreferenceStatus.NEW -> {
                when (next) {
                    ScheduledProofOfDeliveryPreferenceStatus.DONE -> return true
                }
                return false
            }
            ScheduledProofOfDeliveryPreferenceStatus.QUEUED -> return next == ScheduledProofOfDeliveryPreferenceStatus.FAILED
            ScheduledProofOfDeliveryPreferenceStatus.RUNNING -> return next == ScheduledProofOfDeliveryPreferenceStatus.DONE || next == ScheduledProofOfDeliveryPreferenceStatus.FAILED
            ScheduledProofOfDeliveryPreferenceStatus.DONE -> {
                when (next) {
                    ScheduledProofOfDeliveryPreferenceStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledProofOfDeliveryPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("scheduled proof of delivery preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$name]"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): ScheduledProofOfDeliveryPreference {
        return ScheduledProofOfDeliveryPreference(
            code = code,
            name = name,
            title = title,
            checksum = checksum,
            description = description,
            batchSize = batchSize,
            segment = segment,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a scheduled proof of delivery preference in status new. */
fun newScheduledProofOfDeliveryPreference(code: String, name: String): ScheduledProofOfDeliveryPreference {
    return ScheduledProofOfDeliveryPreference(
        code = code,
        name = name,
        title = "",
        checksum = "",
        description = "",
        batchSize = 0,
        segment = "",
        label = "",
        status = ScheduledProofOfDeliveryPreferenceStatus.NEW,
    )
}
