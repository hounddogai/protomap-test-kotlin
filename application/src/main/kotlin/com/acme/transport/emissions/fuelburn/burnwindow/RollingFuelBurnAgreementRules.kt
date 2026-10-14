package com.acme.transport.emissions.fuelburn.burnwindow

/** A rule a rolling fuel burn agreement must pass. Check returns its problems, or none. */
interface RollingFuelBurnAgreementRule {
    fun check(item: RollingFuelBurnAgreement): MutableList<String>
}

data class RollingFuelBurnAgreementWeightRangeRule(
    var min: Double,
    var max: Double,
) : RollingFuelBurnAgreementRule {
    override fun check(item: RollingFuelBurnAgreement): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class RollingFuelBurnAgreementCodePrefixRule(var prefix: String) : RollingFuelBurnAgreementRule {
    override fun check(item: RollingFuelBurnAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnAgreementTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RollingFuelBurnAgreementRule {
    override fun check(item: RollingFuelBurnAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

/** Runs every rule of a rolling fuel burn agreement. */
data class RollingFuelBurnAgreementValidator(var rules: MutableList<RollingFuelBurnAgreementRule>) {
    fun validate(item: RollingFuelBurnAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingFuelBurnAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingFuelBurnAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingFuelBurnAgreementValidator(): RollingFuelBurnAgreementValidator {
    return RollingFuelBurnAgreementValidator(
        mutableListOf<RollingFuelBurnAgreementRule>(
            RollingFuelBurnAgreementWeightRangeRule(1.0, 1424.7),
            RollingFuelBurnAgreementCodePrefixRule("REF"),
            RollingFuelBurnAgreementTagsLimitRule(86400, mutableListOf<String>("tiered", "seasonal")),
        ),
    )
}
