package com.acme.transport.fleetops.defect.defectagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default defect agreement records of fleetops's defect work, and the rules, stores, and calculations
// around them.

/** Where a default defect agreement is in its life cycle. */
enum class DefaultDefectAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default defect agreement as the defect team keeps it. */
data class DefaultDefectAgreement(
    var code: String,
    var name: String,
    var minimum: Double,
    var contactEmail: String,
    var driverId: String,
    var loadKg: Double,
    var retries: Int,
    var description: String,
    var cost: Double,
    var lane: String,
    var category: String,
    var status: DefaultDefectAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDefectAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDefectAgreementStatus): Boolean {
        when (status) {
            DefaultDefectAgreementStatus.CREATED -> {
                when (next) {
                    DefaultDefectAgreementStatus.VALIDATED, DefaultDefectAgreementStatus.RETIRED -> return true
                }
                return false
            }
            DefaultDefectAgreementStatus.VALIDATED -> return next == DefaultDefectAgreementStatus.PUBLISHED || next == DefaultDefectAgreementStatus.RETIRED
            DefaultDefectAgreementStatus.PUBLISHED -> return next == DefaultDefectAgreementStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultDefectAgreementStatus) {
        if (!canMoveTo(next)) {
            error("default defect agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveVolume(): Double = round((retries.toDouble() - loadKg) * 100) / 100

    fun snapshot(): DefaultDefectAgreement {
        return DefaultDefectAgreement(
            code = code,
            name = name,
            minimum = minimum,
            contactEmail = contactEmail,
            driverId = driverId,
            loadKg = loadKg,
            retries = retries,
            description = description,
            cost = cost,
            lane = lane,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default defect agreement in status created. */
fun newDefaultDefectAgreement(code: String, name: String): DefaultDefectAgreement {
    return DefaultDefectAgreement(
        code = code,
        name = name,
        minimum = 0.0,
        contactEmail = "",
        driverId = "",
        loadKg = 0.0,
        retries = 0,
        description = "",
        cost = 0.0,
        lane = "",
        category = "",
        status = DefaultDefectAgreementStatus.CREATED,
    )
}
