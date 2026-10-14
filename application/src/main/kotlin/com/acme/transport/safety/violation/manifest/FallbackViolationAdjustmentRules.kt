package com.acme.transport.safety.violation.manifest

import com.acme.transport.kit.text.normalize

/** A rule a fallback violation adjustment must pass. Check returns its problems, or none. */
interface FallbackViolationAdjustmentRule {
    fun check(item: FallbackViolationAdjustment): MutableList<String>
}

data class FallbackViolationAdjustmentVersionRangeRule(
    var min: Int,
    var max: Int,
) : FallbackViolationAdjustmentRule {
    override fun check(item: FallbackViolationAdjustment): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class FallbackViolationAdjustmentCodeLengthRule(var maxLength: Int) : FallbackViolationAdjustmentRule {
    override fun check(item: FallbackViolationAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackViolationAdjustmentRequiredChannelRule(
    var message: String,
) : FallbackViolationAdjustmentRule {
    override fun check(item: FallbackViolationAdjustment): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackViolationAdjustmentDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : FallbackViolationAdjustmentRule {
    override fun check(item: FallbackViolationAdjustment): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackViolationAdjustmentMinimumRangeRule(
    var min: Double,
    var max: Double,
) : FallbackViolationAdjustmentRule {
    override fun check(item: FallbackViolationAdjustment): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackViolationAdjustmentAllowedChannelRule(
    var allowed: MutableList<String>,
) : FallbackViolationAdjustmentRule {
    override fun check(item: FallbackViolationAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback violation adjustment. */
data class FallbackViolationAdjustmentValidator(var rules: MutableList<FallbackViolationAdjustmentRule>) {
    fun validate(item: FallbackViolationAdjustment): MutableList<String> {
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

    fun requireValid(item: FallbackViolationAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackViolationAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackViolationAdjustmentValidator(): FallbackViolationAdjustmentValidator {
    return FallbackViolationAdjustmentValidator(
        mutableListOf<FallbackViolationAdjustmentRule>(
            FallbackViolationAdjustmentVersionRangeRule(32, 64),
            FallbackViolationAdjustmentCodeLengthRule(120),
            FallbackViolationAdjustmentRequiredChannelRule("fallback violation adjustment needs a value"),
            FallbackViolationAdjustmentDurationMinutesRangeRule(80, 3600),
            FallbackViolationAdjustmentMinimumRangeRule(0.2, 4515.4),
            FallbackViolationAdjustmentAllowedChannelRule(mutableListOf<String>("legacy")),
        ),
    )
}
