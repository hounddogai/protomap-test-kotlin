package com.acme.transport.linehaul.capacity.capacitymapping

import com.acme.transport.kit.text.normalize

/** A rule a scheduled capacity agreement must pass. Check returns its problems, or none. */
interface ScheduledCapacityAgreementRule {
    fun check(item: ScheduledCapacityAgreement): MutableList<String>
}

data class ScheduledCapacityAgreementRequiredCodeRule(var message: String) : ScheduledCapacityAgreementRule {
    override fun check(item: ScheduledCapacityAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCapacityAgreementCurrencyLengthRule(var maxLength: Int) : ScheduledCapacityAgreementRule {
    override fun check(item: ScheduledCapacityAgreement): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCapacityAgreementLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledCapacityAgreementRule {
    override fun check(item: ScheduledCapacityAgreement): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCapacityAgreementAllowedRegionRule(
    var allowed: MutableList<String>,
) : ScheduledCapacityAgreementRule {
    override fun check(item: ScheduledCapacityAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled capacity agreement. */
data class ScheduledCapacityAgreementValidator(var rules: MutableList<ScheduledCapacityAgreementRule>) {
    fun validate(item: ScheduledCapacityAgreement): MutableList<String> {
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

    fun requireValid(item: ScheduledCapacityAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledCapacityAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledCapacityAgreementValidator(): ScheduledCapacityAgreementValidator {
    return ScheduledCapacityAgreementValidator(
        mutableListOf<ScheduledCapacityAgreementRule>(
            ScheduledCapacityAgreementRequiredCodeRule("scheduled capacity agreement needs this field"),
            ScheduledCapacityAgreementCurrencyLengthRule(32),
            ScheduledCapacityAgreementLoadKgRangeRule(2.3, 4996.6),
            ScheduledCapacityAgreementAllowedRegionRule(mutableListOf<String>("regional", "global", "batched")),
        ),
    )
}
