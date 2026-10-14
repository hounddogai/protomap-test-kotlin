package com.acme.transport.fleetops.tire.plan

/** A rule a tire segment must pass. Check returns its problems, or none. */
interface TireSegmentRule {
    fun check(item: TireSegment): MutableList<String>
}

data class TireSegmentCodePrefixRule(var prefix: String) : TireSegmentRule {
    override fun check(item: TireSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TireSegmentRequiredLabelRule(var message: String) : TireSegmentRule {
    override fun check(item: TireSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireSegmentStopCountRangeRule(var min: Int, var max: Int) : TireSegmentRule {
    override fun check(item: TireSegment): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TireSegmentCodeLengthRule(var maxLength: Int) : TireSegmentRule {
    override fun check(item: TireSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireSegmentRequiredDepotRule(var message: String) : TireSegmentRule {
    override fun check(item: TireSegment): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireSegmentNameLengthRule(var maxLength: Int) : TireSegmentRule {
    override fun check(item: TireSegment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire segment. */
data class TireSegmentValidator(var rules: MutableList<TireSegmentRule>) {
    fun validate(item: TireSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultTireSegmentValidator(): TireSegmentValidator {
    return TireSegmentValidator(
        mutableListOf<TireSegmentRule>(
            TireSegmentCodePrefixRule("ACM"),
            TireSegmentRequiredLabelRule("tire segment needs a value"),
            TireSegmentStopCountRangeRule(16, 120),
            TireSegmentCodeLengthRule(86400),
            TireSegmentRequiredDepotRule("tire segment needs an entry"),
            TireSegmentNameLengthRule(1000),
        ),
    )
}
