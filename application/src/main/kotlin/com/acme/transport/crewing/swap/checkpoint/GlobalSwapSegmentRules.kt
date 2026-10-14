package com.acme.transport.crewing.swap.checkpoint

/** A rule a global swap segment must pass. Check returns its problems, or none. */
interface GlobalSwapSegmentRule {
    fun check(item: GlobalSwapSegment): MutableList<String>
}

data class GlobalSwapSegmentRatioRangeRule(var min: Double, var max: Double) : GlobalSwapSegmentRule {
    override fun check(item: GlobalSwapSegment): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalSwapSegmentEtaMinutesRangeRule(var min: Int, var max: Int) : GlobalSwapSegmentRule {
    override fun check(item: GlobalSwapSegment): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class GlobalSwapSegmentCodePrefixRule(var prefix: String) : GlobalSwapSegmentRule {
    override fun check(item: GlobalSwapSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalSwapSegmentRequiredCodeRule(var message: String) : GlobalSwapSegmentRule {
    override fun check(item: GlobalSwapSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global swap segment. */
data class GlobalSwapSegmentValidator(var rules: MutableList<GlobalSwapSegmentRule>) {
    fun validate(item: GlobalSwapSegment): MutableList<String> {
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

    fun requireValid(item: GlobalSwapSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalSwapSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalSwapSegmentValidator(): GlobalSwapSegmentValidator {
    return GlobalSwapSegmentValidator(
        mutableListOf<GlobalSwapSegmentRule>(
            GlobalSwapSegmentRatioRangeRule(3.8, 3635.3),
            GlobalSwapSegmentEtaMinutesRangeRule(16, 16),
            GlobalSwapSegmentCodePrefixRule("X"),
            GlobalSwapSegmentRequiredCodeRule("global swap segment needs this field"),
        ),
    )
}
