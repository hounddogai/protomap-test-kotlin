package com.acme.transport.emissions.evcharge.chargescore

import com.acme.transport.kit.text.normalize

/** A rule a ev charge schedule must pass. Check returns its problems, or none. */
interface EvChargeScheduleRule {
    fun check(item: EvChargeSchedule): MutableList<String>
}

data class EvChargeScheduleBatchSizeRangeRule(var min: Int, var max: Int) : EvChargeScheduleRule {
    override fun check(item: EvChargeSchedule): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class EvChargeScheduleCodeLengthRule(var maxLength: Int) : EvChargeScheduleRule {
    override fun check(item: EvChargeSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeScheduleAllowedCodeRule(var allowed: MutableList<String>) : EvChargeScheduleRule {
    override fun check(item: EvChargeSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeScheduleLabelLengthRule(var maxLength: Int) : EvChargeScheduleRule {
    override fun check(item: EvChargeSchedule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge schedule. */
data class EvChargeScheduleValidator(var rules: MutableList<EvChargeScheduleRule>) {
    fun validate(item: EvChargeSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeScheduleValidator(): EvChargeScheduleValidator {
    return EvChargeScheduleValidator(
        mutableListOf<EvChargeScheduleRule>(
            EvChargeScheduleBatchSizeRangeRule(64, 64),
            EvChargeScheduleCodeLengthRule(64),
            EvChargeScheduleAllowedCodeRule(mutableListOf<String>("fallback", "pending")),
            EvChargeScheduleLabelLengthRule(16),
        ),
    )
}
