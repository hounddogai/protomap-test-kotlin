package com.acme.transport.crewing.availability.adjustment

/** A rule a availability rollup must pass. Check returns its problems, or none. */
interface AvailabilityRollupRule {
    fun check(item: AvailabilityRollup): MutableList<String>
}

data class AvailabilityRollupEndsAtRangeRule(var min: Int, var max: Int) : AvailabilityRollupRule {
    override fun check(item: AvailabilityRollup): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityRollupThresholdRangeRule(var min: Double, var max: Double) : AvailabilityRollupRule {
    override fun check(item: AvailabilityRollup): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class AvailabilityRollupCodeLengthRule(var maxLength: Int) : AvailabilityRollupRule {
    override fun check(item: AvailabilityRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityRollupLabelLengthRule(var maxLength: Int) : AvailabilityRollupRule {
    override fun check(item: AvailabilityRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityRollupCodePrefixRule(var prefix: String) : AvailabilityRollupRule {
    override fun check(item: AvailabilityRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability rollup. */
data class AvailabilityRollupValidator(var rules: MutableList<AvailabilityRollupRule>) {
    fun validate(item: AvailabilityRollup): MutableList<String> {
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

    fun requireValid(item: AvailabilityRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityRollupValidator(): AvailabilityRollupValidator {
    return AvailabilityRollupValidator(
        mutableListOf<AvailabilityRollupRule>(
            AvailabilityRollupEndsAtRangeRule(64, 86400),
            AvailabilityRollupThresholdRangeRule(4.1, 769.6),
            AvailabilityRollupCodeLengthRule(16),
            AvailabilityRollupLabelLengthRule(64),
            AvailabilityRollupCodePrefixRule("REF"),
        ),
    )
}
