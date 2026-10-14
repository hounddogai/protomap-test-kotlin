package com.acme.transport.safety.violation.summary

import com.acme.transport.kit.text.normalize

/** A rule a global violation batch must pass. Check returns its problems, or none. */
interface GlobalViolationBatchRule {
    fun check(item: GlobalViolationBatch): MutableList<String>
}

data class GlobalViolationBatchStopCountRangeRule(var min: Int, var max: Int) : GlobalViolationBatchRule {
    override fun check(item: GlobalViolationBatch): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class GlobalViolationBatchRequiredTrailerIdRule(var message: String) : GlobalViolationBatchRule {
    override fun check(item: GlobalViolationBatch): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalViolationBatchAllowedContactEmailRule(
    var allowed: MutableList<String>,
) : GlobalViolationBatchRule {
    override fun check(item: GlobalViolationBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class GlobalViolationBatchNameLengthRule(var maxLength: Int) : GlobalViolationBatchRule {
    override fun check(item: GlobalViolationBatch): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalViolationBatchTrailerIdLengthRule(var maxLength: Int) : GlobalViolationBatchRule {
    override fun check(item: GlobalViolationBatch): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global violation batch. */
data class GlobalViolationBatchValidator(var rules: MutableList<GlobalViolationBatchRule>) {
    fun validate(item: GlobalViolationBatch): MutableList<String> {
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

    fun requireValid(item: GlobalViolationBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalViolationBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalViolationBatchValidator(): GlobalViolationBatchValidator {
    return GlobalViolationBatchValidator(
        mutableListOf<GlobalViolationBatchRule>(
            GlobalViolationBatchStopCountRangeRule(80, 86400),
            GlobalViolationBatchRequiredTrailerIdRule("global violation batch needs this field"),
            GlobalViolationBatchAllowedContactEmailRule(mutableListOf<String>("weighted", "batched", "legacy")),
            GlobalViolationBatchNameLengthRule(1000),
            GlobalViolationBatchTrailerIdLengthRule(3600),
        ),
    )
}
