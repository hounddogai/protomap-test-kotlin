package com.acme.transport.linehaul.trailer.policy

/** A rule a trailer adjustment must pass. Check returns its problems, or none. */
interface TrailerAdjustmentRule {
    fun check(item: TrailerAdjustment): MutableList<String>
}

data class TrailerAdjustmentCodePrefixRule(var prefix: String) : TrailerAdjustmentRule {
    override fun check(item: TrailerAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerAdjustmentRequiredRegionRule(var message: String) : TrailerAdjustmentRule {
    override fun check(item: TrailerAdjustment): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerAdjustmentWindowDaysRangeRule(var min: Int, var max: Int) : TrailerAdjustmentRule {
    override fun check(item: TrailerAdjustment): MutableList<String> {
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

data class TrailerAdjustmentTitleLengthRule(var maxLength: Int) : TrailerAdjustmentRule {
    override fun check(item: TrailerAdjustment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerAdjustmentRequiredSummaryRule(var message: String) : TrailerAdjustmentRule {
    override fun check(item: TrailerAdjustment): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer adjustment. */
data class TrailerAdjustmentValidator(var rules: MutableList<TrailerAdjustmentRule>) {
    fun validate(item: TrailerAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerAdjustmentValidator(): TrailerAdjustmentValidator {
    return TrailerAdjustmentValidator(
        mutableListOf<TrailerAdjustmentRule>(
            TrailerAdjustmentCodePrefixRule("REF"),
            TrailerAdjustmentRequiredRegionRule("trailer adjustment needs this field"),
            TrailerAdjustmentWindowDaysRangeRule(64, 120),
            TrailerAdjustmentTitleLengthRule(3600),
            TrailerAdjustmentRequiredSummaryRule("trailer adjustment needs an entry"),
        ),
    )
}
