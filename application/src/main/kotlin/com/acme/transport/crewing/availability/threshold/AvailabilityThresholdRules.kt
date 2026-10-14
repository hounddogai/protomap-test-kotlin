package com.acme.transport.crewing.availability.threshold

/** A rule a availability threshold must pass. Check returns its problems, or none. */
interface AvailabilityThresholdRule {
    fun check(item: AvailabilityThreshold): MutableList<String>
}

data class AvailabilityThresholdNameLengthRule(var maxLength: Int) : AvailabilityThresholdRule {
    override fun check(item: AvailabilityThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityThresholdChannelLengthRule(var maxLength: Int) : AvailabilityThresholdRule {
    override fun check(item: AvailabilityThreshold): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityThresholdStartsAtRangeRule(var min: Int, var max: Int) : AvailabilityThresholdRule {
    override fun check(item: AvailabilityThreshold): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityThresholdCodePrefixRule(var prefix: String) : AvailabilityThresholdRule {
    override fun check(item: AvailabilityThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityThresholdRequiredCodeRule(var message: String) : AvailabilityThresholdRule {
    override fun check(item: AvailabilityThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability threshold. */
data class AvailabilityThresholdValidator(var rules: MutableList<AvailabilityThresholdRule>) {
    fun validate(item: AvailabilityThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AvailabilityThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityThresholdValidator(): AvailabilityThresholdValidator {
    return AvailabilityThresholdValidator(
        mutableListOf<AvailabilityThresholdRule>(
            AvailabilityThresholdNameLengthRule(120),
            AvailabilityThresholdChannelLengthRule(255),
            AvailabilityThresholdStartsAtRangeRule(16, 16),
            AvailabilityThresholdCodePrefixRule("OPS"),
            AvailabilityThresholdRequiredCodeRule("availability threshold needs an entry"),
        ),
    )
}
