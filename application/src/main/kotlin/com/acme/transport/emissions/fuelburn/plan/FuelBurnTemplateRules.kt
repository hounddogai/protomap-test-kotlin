package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.kit.text.normalize

/** A rule a fuel burn template must pass. Check returns its problems, or none. */
interface FuelBurnTemplateRule {
    fun check(item: FuelBurnTemplate): MutableList<String>
}

data class FuelBurnTemplateStartsAtRangeRule(var min: Int, var max: Int) : FuelBurnTemplateRule {
    override fun check(item: FuelBurnTemplate): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnTemplatePhoneLengthRule(var maxLength: Int) : FuelBurnTemplateRule {
    override fun check(item: FuelBurnTemplate): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnTemplateApprovedGuardRule(var limit: Int) : FuelBurnTemplateRule {
    override fun check(item: FuelBurnTemplate): MutableList<String> {
        if (item.approved && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnTemplateAllowedPhoneRule(var allowed: MutableList<String>) : FuelBurnTemplateRule {
    override fun check(item: FuelBurnTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnTemplateRequiredRegionRule(var message: String) : FuelBurnTemplateRule {
    override fun check(item: FuelBurnTemplate): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnTemplateRegionLengthRule(var maxLength: Int) : FuelBurnTemplateRule {
    override fun check(item: FuelBurnTemplate): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn template. */
data class FuelBurnTemplateValidator(var rules: MutableList<FuelBurnTemplateRule>) {
    fun validate(item: FuelBurnTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnTemplateValidator(): FuelBurnTemplateValidator {
    return FuelBurnTemplateValidator(
        mutableListOf<FuelBurnTemplateRule>(
            FuelBurnTemplateStartsAtRangeRule(16, 86400),
            FuelBurnTemplatePhoneLengthRule(64),
            FuelBurnTemplateApprovedGuardRule(255),
            FuelBurnTemplateAllowedPhoneRule(mutableListOf<String>("batched", "strict")),
            FuelBurnTemplateRequiredRegionRule("fuel burn template needs this field"),
            FuelBurnTemplateRegionLengthRule(64),
        ),
    )
}
