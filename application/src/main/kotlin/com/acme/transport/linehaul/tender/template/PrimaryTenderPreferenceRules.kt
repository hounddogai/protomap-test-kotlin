package com.acme.transport.linehaul.tender.template

import com.acme.transport.kit.text.normalize

/** A rule a primary tender preference must pass. Check returns its problems, or none. */
interface PrimaryTenderPreferenceRule {
    fun check(item: PrimaryTenderPreference): MutableList<String>
}

data class PrimaryTenderPreferenceAllowedSummaryRule(
    var allowed: MutableList<String>,
) : PrimaryTenderPreferenceRule {
    override fun check(item: PrimaryTenderPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTenderPreferenceCodeLengthRule(var maxLength: Int) : PrimaryTenderPreferenceRule {
    override fun check(item: PrimaryTenderPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTenderPreferenceRateRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryTenderPreferenceRule {
    override fun check(item: PrimaryTenderPreference): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class PrimaryTenderPreferenceSummaryLengthRule(var maxLength: Int) : PrimaryTenderPreferenceRule {
    override fun check(item: PrimaryTenderPreference): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTenderPreferenceDiscountRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryTenderPreferenceRule {
    override fun check(item: PrimaryTenderPreference): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTenderPreferenceRequiredCodeRule(var message: String) : PrimaryTenderPreferenceRule {
    override fun check(item: PrimaryTenderPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary tender preference. */
data class PrimaryTenderPreferenceValidator(var rules: MutableList<PrimaryTenderPreferenceRule>) {
    fun validate(item: PrimaryTenderPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryTenderPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTenderPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTenderPreferenceValidator(): PrimaryTenderPreferenceValidator {
    return PrimaryTenderPreferenceValidator(
        mutableListOf<PrimaryTenderPreferenceRule>(
            PrimaryTenderPreferenceAllowedSummaryRule(mutableListOf<String>()),
            PrimaryTenderPreferenceCodeLengthRule(16),
            PrimaryTenderPreferenceRateRangeRule(8.8, 212.3),
            PrimaryTenderPreferenceSummaryLengthRule(80),
            PrimaryTenderPreferenceDiscountRangeRule(3.0, 2255.3),
            PrimaryTenderPreferenceRequiredCodeRule("primary tender preference needs this field"),
        ),
    )
}
