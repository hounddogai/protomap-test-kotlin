package com.acme.transport.terminal.traileryard.schedule

/** A rule a trailer yard rollup must pass. Check returns its problems, or none. */
interface TrailerYardRollupRule {
    fun check(item: TrailerYardRollup): MutableList<String>
}

data class TrailerYardRollupRequiredNameRule(var message: String) : TrailerYardRollupRule {
    override fun check(item: TrailerYardRollup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardRollupNameLengthRule(var maxLength: Int) : TrailerYardRollupRule {
    override fun check(item: TrailerYardRollup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardRollupCodeLengthRule(var maxLength: Int) : TrailerYardRollupRule {
    override fun check(item: TrailerYardRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardRollupCodePrefixRule(var prefix: String) : TrailerYardRollupRule {
    override fun check(item: TrailerYardRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardRollupUpdatedAtRangeRule(var min: Int, var max: Int) : TrailerYardRollupRule {
    override fun check(item: TrailerYardRollup): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a trailer yard rollup. */
data class TrailerYardRollupValidator(var rules: MutableList<TrailerYardRollupRule>) {
    fun validate(item: TrailerYardRollup): MutableList<String> {
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

    fun requireValid(item: TrailerYardRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardRollupValidator(): TrailerYardRollupValidator {
    return TrailerYardRollupValidator(
        mutableListOf<TrailerYardRollupRule>(
            TrailerYardRollupRequiredNameRule("trailer yard rollup needs this field"),
            TrailerYardRollupNameLengthRule(64),
            TrailerYardRollupCodeLengthRule(120),
            TrailerYardRollupCodePrefixRule("ACM"),
            TrailerYardRollupUpdatedAtRangeRule(32, 255),
        ),
    )
}
