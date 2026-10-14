package com.acme.transport.linehaul.capacity.capacitysnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default capacity preference is in its life cycle. */
enum class DefaultCapacityPreferenceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One default capacity preference, with what the planners need to act on it. */
data class DefaultCapacityPreference(
    var code: String,
    var title: String,
    var name: String,
    var updatedAt: Int,
    var country: String,
    var source: String,
    var batchSize: Int,
    var currency: String,
    var status: DefaultCapacityPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCapacityPreferenceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCapacityPreferenceStatus): Boolean {
        when (status) {
            DefaultCapacityPreferenceStatus.CREATED -> return next == DefaultCapacityPreferenceStatus.RETIRED
            DefaultCapacityPreferenceStatus.VALIDATED -> return next == DefaultCapacityPreferenceStatus.PUBLISHED
            DefaultCapacityPreferenceStatus.PUBLISHED -> {
                when (next) {
                    DefaultCapacityPreferenceStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultCapacityPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("default capacity preference $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedLoad(): Double = round(batchSize.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): DefaultCapacityPreference {
        return DefaultCapacityPreference(
            code = code,
            title = title,
            name = name,
            updatedAt = updatedAt,
            country = country,
            source = source,
            batchSize = batchSize,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a default capacity preference in status created. */
fun newDefaultCapacityPreference(code: String, title: String): DefaultCapacityPreference {
    return DefaultCapacityPreference(
        code = code,
        title = title,
        name = "",
        updatedAt = 0,
        country = "",
        source = "",
        batchSize = 0,
        currency = "",
        status = DefaultCapacityPreferenceStatus.CREATED,
    )
}
