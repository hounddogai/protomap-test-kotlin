package com.acme.transport.terminal.crossdock.summary

/** A rule a cross dock override must pass. Check returns its problems, or none. */
interface CrossDockOverrideRule {
    fun check(item: CrossDockOverride): MutableList<String>
}

data class CrossDockOverrideCodePrefixRule(var prefix: String) : CrossDockOverrideRule {
    override fun check(item: CrossDockOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockOverrideRequiredCodeRule(var message: String) : CrossDockOverrideRule {
    override fun check(item: CrossDockOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockOverrideCodeLengthRule(var maxLength: Int) : CrossDockOverrideRule {
    override fun check(item: CrossDockOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockOverrideWeightRangeRule(var min: Double, var max: Double) : CrossDockOverrideRule {
    override fun check(item: CrossDockOverride): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock override. */
data class CrossDockOverrideValidator(var rules: MutableList<CrossDockOverrideRule>) {
    fun validate(item: CrossDockOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockOverrideValidator(): CrossDockOverrideValidator {
    return CrossDockOverrideValidator(
        mutableListOf<CrossDockOverrideRule>(
            CrossDockOverrideCodePrefixRule("Q"),
            CrossDockOverrideRequiredCodeRule("cross dock override needs an entry"),
            CrossDockOverrideCodeLengthRule(1000),
            CrossDockOverrideWeightRangeRule(4.8, 1250.4),
        ),
    )
}
