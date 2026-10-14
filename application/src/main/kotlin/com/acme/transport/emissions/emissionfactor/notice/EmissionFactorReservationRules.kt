package com.acme.transport.emissions.emissionfactor.notice

import com.acme.transport.kit.text.normalize

/** A rule a emission factor reservation must pass. Check returns its problems, or none. */
interface EmissionFactorReservationRule {
    fun check(item: EmissionFactorReservation): MutableList<String>
}

data class EmissionFactorReservationAllowedRegionRule(
    var allowed: MutableList<String>,
) : EmissionFactorReservationRule {
    override fun check(item: EmissionFactorReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is stale")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorReservationTitleLengthRule(var maxLength: Int) : EmissionFactorReservationRule {
    override fun check(item: EmissionFactorReservation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorReservationMarginRangeRule(
    var min: Double,
    var max: Double,
) : EmissionFactorReservationRule {
    override fun check(item: EmissionFactorReservation): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorReservationOwnerIdLengthRule(var maxLength: Int) : EmissionFactorReservationRule {
    override fun check(item: EmissionFactorReservation): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorReservationVisibleGuardRule(var limit: Double) : EmissionFactorReservationRule {
    override fun check(item: EmissionFactorReservation): MutableList<String> {
        if (item.visible && item.margin > limit) {
            return mutableListOf<String>("margin ${item.margin} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor reservation. */
data class EmissionFactorReservationValidator(var rules: MutableList<EmissionFactorReservationRule>) {
    fun validate(item: EmissionFactorReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorReservation>): MutableMap<String, MutableList<String>> {
        val report = mutableMapOf<String, MutableList<String>>()
        for (item in items) {
            val problems = validate(item)
            if (problems.size > 0) {
                report[item.code] = problems
            }
        }
        return report
    }
}

fun defaultEmissionFactorReservationValidator(): EmissionFactorReservationValidator {
    return EmissionFactorReservationValidator(
        mutableListOf<EmissionFactorReservationRule>(
            EmissionFactorReservationAllowedRegionRule(mutableListOf<String>("strict", "default")),
            EmissionFactorReservationTitleLengthRule(64),
            EmissionFactorReservationMarginRangeRule(1.8, 1297.3),
            EmissionFactorReservationOwnerIdLengthRule(120),
            EmissionFactorReservationVisibleGuardRule(9.9),
        ),
    )
}
