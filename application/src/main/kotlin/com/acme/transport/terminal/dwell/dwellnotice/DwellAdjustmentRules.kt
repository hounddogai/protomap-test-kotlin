package com.acme.transport.terminal.dwell.dwellnotice

import com.acme.transport.kit.text.normalize

/** A rule a dwell adjustment must pass. Check returns its problems, or none. */
interface DwellAdjustmentRule {
    fun check(item: DwellAdjustment): MutableList<String>
}

data class DwellAdjustmentRequiredNameRule(var message: String) : DwellAdjustmentRule {
    override fun check(item: DwellAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellAdjustmentCodePrefixRule(var prefix: String) : DwellAdjustmentRule {
    override fun check(item: DwellAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DwellAdjustmentCodeLengthRule(var maxLength: Int) : DwellAdjustmentRule {
    override fun check(item: DwellAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellAdjustmentSummaryLengthRule(var maxLength: Int) : DwellAdjustmentRule {
    override fun check(item: DwellAdjustment): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellAdjustmentPriorityRangeRule(var min: Int, var max: Int) : DwellAdjustmentRule {
    override fun check(item: DwellAdjustment): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DwellAdjustmentAllowedCodeRule(var allowed: MutableList<String>) : DwellAdjustmentRule {
    override fun check(item: DwellAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell adjustment. */
data class DwellAdjustmentValidator(var rules: MutableList<DwellAdjustmentRule>) {
    fun validate(item: DwellAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DwellAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellAdjustmentValidator(): DwellAdjustmentValidator {
    return DwellAdjustmentValidator(
        mutableListOf<DwellAdjustmentRule>(
            DwellAdjustmentRequiredNameRule("dwell adjustment needs an entry"),
            DwellAdjustmentCodePrefixRule("X"),
            DwellAdjustmentCodeLengthRule(255),
            DwellAdjustmentSummaryLengthRule(64),
            DwellAdjustmentPriorityRangeRule(80, 1000),
            DwellAdjustmentAllowedCodeRule(mutableListOf<String>("tiered", "capped")),
        ),
    )
}
