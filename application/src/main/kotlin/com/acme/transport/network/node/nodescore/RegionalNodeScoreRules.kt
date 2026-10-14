package com.acme.transport.network.node.nodescore

/** A rule a regional node score must pass. Check returns its problems, or none. */
interface RegionalNodeScoreRule {
    fun check(item: RegionalNodeScore): MutableList<String>
}

data class RegionalNodeScoreWindowDaysRangeRule(var min: Int, var max: Int) : RegionalNodeScoreRule {
    override fun check(item: RegionalNodeScore): MutableList<String> {
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

data class RegionalNodeScoreRequiredNameRule(var message: String) : RegionalNodeScoreRule {
    override fun check(item: RegionalNodeScore): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalNodeScoreTimezoneLengthRule(var maxLength: Int) : RegionalNodeScoreRule {
    override fun check(item: RegionalNodeScore): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalNodeScoreRequiredCodeRule(var message: String) : RegionalNodeScoreRule {
    override fun check(item: RegionalNodeScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalNodeScoreFloorValueRangeRule(var min: Double, var max: Double) : RegionalNodeScoreRule {
    override fun check(item: RegionalNodeScore): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional node score. */
data class RegionalNodeScoreValidator(var rules: MutableList<RegionalNodeScoreRule>) {
    fun validate(item: RegionalNodeScore): MutableList<String> {
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

    fun requireValid(item: RegionalNodeScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalNodeScore>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalNodeScoreValidator(): RegionalNodeScoreValidator {
    return RegionalNodeScoreValidator(
        mutableListOf<RegionalNodeScoreRule>(
            RegionalNodeScoreWindowDaysRangeRule(120, 86400),
            RegionalNodeScoreRequiredNameRule("regional node score needs this field"),
            RegionalNodeScoreTimezoneLengthRule(32),
            RegionalNodeScoreRequiredCodeRule("regional node score needs a value"),
            RegionalNodeScoreFloorValueRangeRule(1.9, 3003.6),
        ),
    )
}
