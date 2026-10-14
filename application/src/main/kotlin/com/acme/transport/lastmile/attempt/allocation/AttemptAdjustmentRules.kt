package com.acme.transport.lastmile.attempt.allocation

import com.acme.transport.kit.text.normalize

/** A rule a attempt adjustment must pass. Check returns its problems, or none. */
interface AttemptAdjustmentRule {
    fun check(item: AttemptAdjustment): MutableList<String>
}

data class AttemptAdjustmentChannelLengthRule(var maxLength: Int) : AttemptAdjustmentRule {
    override fun check(item: AttemptAdjustment): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptAdjustmentAllowedLabelRule(var allowed: MutableList<String>) : AttemptAdjustmentRule {
    override fun check(item: AttemptAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class AttemptAdjustmentCeilingRangeRule(var min: Double, var max: Double) : AttemptAdjustmentRule {
    override fun check(item: AttemptAdjustment): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AttemptAdjustmentLabelLengthRule(var maxLength: Int) : AttemptAdjustmentRule {
    override fun check(item: AttemptAdjustment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt adjustment. */
data class AttemptAdjustmentValidator(var rules: MutableList<AttemptAdjustmentRule>) {
    fun validate(item: AttemptAdjustment): MutableList<String> {
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

    fun requireValid(item: AttemptAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptAdjustmentValidator(): AttemptAdjustmentValidator {
    return AttemptAdjustmentValidator(
        mutableListOf<AttemptAdjustmentRule>(
            AttemptAdjustmentChannelLengthRule(120),
            AttemptAdjustmentAllowedLabelRule(mutableListOf<String>("seasonal", "scheduled")),
            AttemptAdjustmentCeilingRangeRule(8.7, 1177.4),
            AttemptAdjustmentLabelLengthRule(64),
        ),
    )
}
