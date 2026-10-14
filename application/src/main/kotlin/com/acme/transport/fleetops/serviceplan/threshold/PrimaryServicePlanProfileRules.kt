package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.text.normalize

/** A rule a primary service plan profile must pass. Check returns its problems, or none. */
interface PrimaryServicePlanProfileRule {
    fun check(item: PrimaryServicePlanProfile): MutableList<String>
}

data class PrimaryServicePlanProfileEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryServicePlanProfileRule {
    override fun check(item: PrimaryServicePlanProfile): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class PrimaryServicePlanProfileShiftCodeLengthRule(var maxLength: Int) : PrimaryServicePlanProfileRule {
    override fun check(item: PrimaryServicePlanProfile): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServicePlanProfileArchivedGuardRule(var limit: Double) : PrimaryServicePlanProfileRule {
    override fun check(item: PrimaryServicePlanProfile): MutableList<String> {
        if (item.archived && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServicePlanProfileAllowedCodeRule(
    var allowed: MutableList<String>,
) : PrimaryServicePlanProfileRule {
    override fun check(item: PrimaryServicePlanProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServicePlanProfileRequiredShiftCodeRule(var message: String) : PrimaryServicePlanProfileRule {
    override fun check(item: PrimaryServicePlanProfile): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServicePlanProfileRequiredCodeRule(var message: String) : PrimaryServicePlanProfileRule {
    override fun check(item: PrimaryServicePlanProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary service plan profile. */
data class PrimaryServicePlanProfileValidator(var rules: MutableList<PrimaryServicePlanProfileRule>) {
    fun validate(item: PrimaryServicePlanProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryServicePlanProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryServicePlanProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryServicePlanProfileValidator(): PrimaryServicePlanProfileValidator {
    return PrimaryServicePlanProfileValidator(
        mutableListOf<PrimaryServicePlanProfileRule>(
            PrimaryServicePlanProfileEtaMinutesRangeRule(80, 1000),
            PrimaryServicePlanProfileShiftCodeLengthRule(80),
            PrimaryServicePlanProfileArchivedGuardRule(4.8),
            PrimaryServicePlanProfileAllowedCodeRule(mutableListOf<String>("tiered")),
            PrimaryServicePlanProfileRequiredShiftCodeRule("primary service plan profile needs an entry"),
            PrimaryServicePlanProfileRequiredCodeRule("primary service plan profile needs an entry"),
        ),
    )
}
