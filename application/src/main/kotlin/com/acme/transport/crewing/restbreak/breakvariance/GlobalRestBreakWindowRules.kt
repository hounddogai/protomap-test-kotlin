package com.acme.transport.crewing.restbreak.breakvariance

/** A rule a global rest break window must pass. Check returns its problems, or none. */
interface GlobalRestBreakWindowRule {
    fun check(item: GlobalRestBreakWindow): MutableList<String>
}

data class GlobalRestBreakWindowRequiredNameRule(var message: String) : GlobalRestBreakWindowRule {
    override fun check(item: GlobalRestBreakWindow): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRestBreakWindowRequiredCodeRule(var message: String) : GlobalRestBreakWindowRule {
    override fun check(item: GlobalRestBreakWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRestBreakWindowNameLengthRule(var maxLength: Int) : GlobalRestBreakWindowRule {
    override fun check(item: GlobalRestBreakWindow): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRestBreakWindowCodePrefixRule(var prefix: String) : GlobalRestBreakWindowRule {
    override fun check(item: GlobalRestBreakWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRestBreakWindowCostRangeRule(var min: Double, var max: Double) : GlobalRestBreakWindowRule {
    override fun check(item: GlobalRestBreakWindow): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRestBreakWindowWindowDaysRangeRule(var min: Int, var max: Int) : GlobalRestBreakWindowRule {
    override fun check(item: GlobalRestBreakWindow): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global rest break window. */
data class GlobalRestBreakWindowValidator(var rules: MutableList<GlobalRestBreakWindowRule>) {
    fun validate(item: GlobalRestBreakWindow): MutableList<String> {
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

    fun requireValid(item: GlobalRestBreakWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRestBreakWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRestBreakWindowValidator(): GlobalRestBreakWindowValidator {
    return GlobalRestBreakWindowValidator(
        mutableListOf<GlobalRestBreakWindowRule>(
            GlobalRestBreakWindowRequiredNameRule("global rest break window needs an entry"),
            GlobalRestBreakWindowRequiredCodeRule("global rest break window needs an entry"),
            GlobalRestBreakWindowNameLengthRule(1000),
            GlobalRestBreakWindowCodePrefixRule("REF"),
            GlobalRestBreakWindowCostRangeRule(6.3, 1715.8),
            GlobalRestBreakWindowWindowDaysRangeRule(255, 3600),
        ),
    )
}
