package com.acme.transport.linehaul.trailer.recorditem

/** A rule a trailer record must pass. Check returns its problems, or none. */
interface TrailerRecordRule {
    fun check(item: TrailerRecord): MutableList<String>
}

data class TrailerRecordLabelLengthRule(var maxLength: Int) : TrailerRecordRule {
    override fun check(item: TrailerRecord): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerRecordRouteCodeLengthRule(var maxLength: Int) : TrailerRecordRule {
    override fun check(item: TrailerRecord): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerRecordCodeLengthRule(var maxLength: Int) : TrailerRecordRule {
    override fun check(item: TrailerRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerRecordCodePrefixRule(var prefix: String) : TrailerRecordRule {
    override fun check(item: TrailerRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerRecordThresholdRangeRule(var min: Double, var max: Double) : TrailerRecordRule {
    override fun check(item: TrailerRecord): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a trailer record. */
data class TrailerRecordValidator(var rules: MutableList<TrailerRecordRule>) {
    fun validate(item: TrailerRecord): MutableList<String> {
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

    fun requireValid(item: TrailerRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerRecordValidator(): TrailerRecordValidator {
    return TrailerRecordValidator(
        mutableListOf<TrailerRecordRule>(
            TrailerRecordLabelLengthRule(255),
            TrailerRecordRouteCodeLengthRule(255),
            TrailerRecordCodeLengthRule(120),
            TrailerRecordCodePrefixRule("X"),
            TrailerRecordThresholdRangeRule(1.1, 3310.4),
        ),
    )
}
