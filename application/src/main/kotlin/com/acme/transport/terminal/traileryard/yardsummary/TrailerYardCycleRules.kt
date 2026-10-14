package com.acme.transport.terminal.traileryard.yardsummary

/** A rule a trailer yard cycle must pass. Check returns its problems, or none. */
interface TrailerYardCycleRule {
    fun check(item: TrailerYardCycle): MutableList<String>
}

data class TrailerYardCycleRequiredCodeRule(var message: String) : TrailerYardCycleRule {
    override fun check(item: TrailerYardCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardCycleCodeLengthRule(var maxLength: Int) : TrailerYardCycleRule {
    override fun check(item: TrailerYardCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardCyclePriorityRangeRule(var min: Int, var max: Int) : TrailerYardCycleRule {
    override fun check(item: TrailerYardCycle): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardCycleCodePrefixRule(var prefix: String) : TrailerYardCycleRule {
    override fun check(item: TrailerYardCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard cycle. */
data class TrailerYardCycleValidator(var rules: MutableList<TrailerYardCycleRule>) {
    fun validate(item: TrailerYardCycle): MutableList<String> {
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

    fun requireValid(item: TrailerYardCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardCycleValidator(): TrailerYardCycleValidator {
    return TrailerYardCycleValidator(
        mutableListOf<TrailerYardCycleRule>(
            TrailerYardCycleRequiredCodeRule("trailer yard cycle needs this field"),
            TrailerYardCycleCodeLengthRule(86400),
            TrailerYardCyclePriorityRangeRule(32, 64),
            TrailerYardCycleCodePrefixRule("X"),
        ),
    )
}
