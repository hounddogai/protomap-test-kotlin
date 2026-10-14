package com.acme.transport.fleetops.serviceplan.variance

import com.acme.transport.kit.text.normalize

/** A rule a legacy service plan variance must pass. Check returns its problems, or none. */
interface LegacyServicePlanVarianceRule {
    fun check(item: LegacyServicePlanVariance): MutableList<String>
}

data class LegacyServicePlanVarianceRequiredNameRule(var message: String) : LegacyServicePlanVarianceRule {
    override fun check(item: LegacyServicePlanVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanVarianceRequiredTimezoneRule(var message: String) : LegacyServicePlanVarianceRule {
    override fun check(item: LegacyServicePlanVariance): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanVarianceCodeLengthRule(var maxLength: Int) : LegacyServicePlanVarianceRule {
    override fun check(item: LegacyServicePlanVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanVarianceTimezoneLengthRule(var maxLength: Int) : LegacyServicePlanVarianceRule {
    override fun check(item: LegacyServicePlanVariance): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanVarianceAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : LegacyServicePlanVarianceRule {
    override fun check(item: LegacyServicePlanVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is too long")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanVarianceCodePrefixRule(var prefix: String) : LegacyServicePlanVarianceRule {
    override fun check(item: LegacyServicePlanVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy service plan variance. */
data class LegacyServicePlanVarianceValidator(var rules: MutableList<LegacyServicePlanVarianceRule>) {
    fun validate(item: LegacyServicePlanVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: LegacyServicePlanVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyServicePlanVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyServicePlanVarianceValidator(): LegacyServicePlanVarianceValidator {
    return LegacyServicePlanVarianceValidator(
        mutableListOf<LegacyServicePlanVarianceRule>(
            LegacyServicePlanVarianceRequiredNameRule("legacy service plan variance needs this field"),
            LegacyServicePlanVarianceRequiredTimezoneRule("legacy service plan variance needs this field"),
            LegacyServicePlanVarianceCodeLengthRule(120),
            LegacyServicePlanVarianceTimezoneLengthRule(3600),
            LegacyServicePlanVarianceAllowedTrailerIdRule(
                mutableListOf<String>("batched", "primary", "weighted"),
            ),
            LegacyServicePlanVarianceCodePrefixRule("REF"),
        ),
    )
}
