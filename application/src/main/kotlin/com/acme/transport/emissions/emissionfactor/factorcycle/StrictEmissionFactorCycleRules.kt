package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.kit.text.normalize

/** A rule a strict emission factor cycle must pass. Check returns its problems, or none. */
interface StrictEmissionFactorCycleRule {
    fun check(item: StrictEmissionFactorCycle): MutableList<String>
}

data class StrictEmissionFactorCycleAllowedContactEmailRule(
    var allowed: MutableList<String>,
) : StrictEmissionFactorCycleRule {
    override fun check(item: StrictEmissionFactorCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class StrictEmissionFactorCycleDepotLengthRule(var maxLength: Int) : StrictEmissionFactorCycleRule {
    override fun check(item: StrictEmissionFactorCycle): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictEmissionFactorCycleRequiredContactEmailRule(
    var message: String,
) : StrictEmissionFactorCycleRule {
    override fun check(item: StrictEmissionFactorCycle): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictEmissionFactorCycleRequiredDepotRule(var message: String) : StrictEmissionFactorCycleRule {
    override fun check(item: StrictEmissionFactorCycle): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictEmissionFactorCycleTitleLengthRule(var maxLength: Int) : StrictEmissionFactorCycleRule {
    override fun check(item: StrictEmissionFactorCycle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict emission factor cycle. */
data class StrictEmissionFactorCycleValidator(var rules: MutableList<StrictEmissionFactorCycleRule>) {
    fun validate(item: StrictEmissionFactorCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictEmissionFactorCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictEmissionFactorCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictEmissionFactorCycleValidator(): StrictEmissionFactorCycleValidator {
    return StrictEmissionFactorCycleValidator(
        mutableListOf<StrictEmissionFactorCycleRule>(
            StrictEmissionFactorCycleAllowedContactEmailRule(
                mutableListOf<String>("capped", "manual", "capped"),
            ),
            StrictEmissionFactorCycleDepotLengthRule(3600),
            StrictEmissionFactorCycleRequiredContactEmailRule("strict emission factor cycle needs this field"),
            StrictEmissionFactorCycleRequiredDepotRule("strict emission factor cycle needs this field"),
            StrictEmissionFactorCycleTitleLengthRule(1000),
        ),
    )
}
