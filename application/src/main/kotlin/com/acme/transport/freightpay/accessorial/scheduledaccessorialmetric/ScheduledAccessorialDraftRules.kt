package com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric

import com.acme.transport.kit.text.normalize

/** A rule a scheduled accessorial draft must pass. Check returns its problems, or none. */
interface ScheduledAccessorialDraftRule {
    fun check(item: ScheduledAccessorialDraft): MutableList<String>
}

data class ScheduledAccessorialDraftFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledAccessorialDraftRule {
    override fun check(item: ScheduledAccessorialDraft): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAccessorialDraftCodeLengthRule(var maxLength: Int) : ScheduledAccessorialDraftRule {
    override fun check(item: ScheduledAccessorialDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAccessorialDraftAllowedNameRule(
    var allowed: MutableList<String>,
) : ScheduledAccessorialDraftRule {
    override fun check(item: ScheduledAccessorialDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled accessorial draft. */
data class ScheduledAccessorialDraftValidator(var rules: MutableList<ScheduledAccessorialDraftRule>) {
    fun validate(item: ScheduledAccessorialDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledAccessorialDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledAccessorialDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledAccessorialDraftValidator(): ScheduledAccessorialDraftValidator {
    return ScheduledAccessorialDraftValidator(
        mutableListOf<ScheduledAccessorialDraftRule>(
            ScheduledAccessorialDraftFuelLitersRangeRule(6.7, 2417.7),
            ScheduledAccessorialDraftCodeLengthRule(64),
            ScheduledAccessorialDraftAllowedNameRule(mutableListOf<String>()),
        ),
    )
}
