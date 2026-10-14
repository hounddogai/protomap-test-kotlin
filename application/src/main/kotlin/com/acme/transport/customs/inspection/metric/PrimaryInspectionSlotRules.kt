package com.acme.transport.customs.inspection.metric

import com.acme.transport.kit.text.normalize

/** A rule a primary inspection slot must pass. Check returns its problems, or none. */
interface PrimaryInspectionSlotRule {
    fun check(item: PrimaryInspectionSlot): MutableList<String>
}

data class PrimaryInspectionSlotGraceDaysRangeRule(var min: Int, var max: Int) : PrimaryInspectionSlotRule {
    override fun check(item: PrimaryInspectionSlot): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionSlotRetriesRangeRule(var min: Int, var max: Int) : PrimaryInspectionSlotRule {
    override fun check(item: PrimaryInspectionSlot): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionSlotCodePrefixRule(var prefix: String) : PrimaryInspectionSlotRule {
    override fun check(item: PrimaryInspectionSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionSlotAllowedCodeRule(var allowed: MutableList<String>) : PrimaryInspectionSlotRule {
    override fun check(item: PrimaryInspectionSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionSlotSummaryLengthRule(var maxLength: Int) : PrimaryInspectionSlotRule {
    override fun check(item: PrimaryInspectionSlot): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary inspection slot. */
data class PrimaryInspectionSlotValidator(var rules: MutableList<PrimaryInspectionSlotRule>) {
    fun validate(item: PrimaryInspectionSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryInspectionSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryInspectionSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryInspectionSlotValidator(): PrimaryInspectionSlotValidator {
    return PrimaryInspectionSlotValidator(
        mutableListOf<PrimaryInspectionSlotRule>(
            PrimaryInspectionSlotGraceDaysRangeRule(120, 3600),
            PrimaryInspectionSlotRetriesRangeRule(80, 86400),
            PrimaryInspectionSlotCodePrefixRule("X"),
            PrimaryInspectionSlotAllowedCodeRule(mutableListOf<String>()),
            PrimaryInspectionSlotSummaryLengthRule(255),
        ),
    )
}
