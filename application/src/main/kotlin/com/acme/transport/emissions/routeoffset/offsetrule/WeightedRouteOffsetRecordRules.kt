package com.acme.transport.emissions.routeoffset.offsetrule

/** A rule a weighted route offset record must pass. Check returns its problems, or none. */
interface WeightedRouteOffsetRecordRule {
    fun check(item: WeightedRouteOffsetRecord): MutableList<String>
}

data class WeightedRouteOffsetRecordVersionRangeRule(
    var min: Int,
    var max: Int,
) : WeightedRouteOffsetRecordRule {
    override fun check(item: WeightedRouteOffsetRecord): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRouteOffsetRecordPriorityRangeRule(
    var min: Int,
    var max: Int,
) : WeightedRouteOffsetRecordRule {
    override fun check(item: WeightedRouteOffsetRecord): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRouteOffsetRecordUnitLengthRule(var maxLength: Int) : WeightedRouteOffsetRecordRule {
    override fun check(item: WeightedRouteOffsetRecord): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRouteOffsetRecordRequiredCodeRule(var message: String) : WeightedRouteOffsetRecordRule {
    override fun check(item: WeightedRouteOffsetRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedRouteOffsetRecordCodePrefixRule(var prefix: String) : WeightedRouteOffsetRecordRule {
    override fun check(item: WeightedRouteOffsetRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRouteOffsetRecordLabelLengthRule(var maxLength: Int) : WeightedRouteOffsetRecordRule {
    override fun check(item: WeightedRouteOffsetRecord): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted route offset record. */
data class WeightedRouteOffsetRecordValidator(var rules: MutableList<WeightedRouteOffsetRecordRule>) {
    fun validate(item: WeightedRouteOffsetRecord): MutableList<String> {
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

    fun requireValid(item: WeightedRouteOffsetRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedRouteOffsetRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedRouteOffsetRecordValidator(): WeightedRouteOffsetRecordValidator {
    return WeightedRouteOffsetRecordValidator(
        mutableListOf<WeightedRouteOffsetRecordRule>(
            WeightedRouteOffsetRecordVersionRangeRule(16, 1000),
            WeightedRouteOffsetRecordPriorityRangeRule(80, 120),
            WeightedRouteOffsetRecordUnitLengthRule(80),
            WeightedRouteOffsetRecordRequiredCodeRule("weighted route offset record needs an entry"),
            WeightedRouteOffsetRecordCodePrefixRule("ACM"),
            WeightedRouteOffsetRecordLabelLengthRule(1000),
        ),
    )
}
