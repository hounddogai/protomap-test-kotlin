package com.acme.transport.linehaul.backhaul.backhaulbatch

/** A rule a backhaul baseline must pass. Check returns its problems, or none. */
interface BackhaulBaselineRule {
    fun check(item: BackhaulBaseline): MutableList<String>
}

data class BackhaulBaselineRequiredReviewerRule(var message: String) : BackhaulBaselineRule {
    override fun check(item: BackhaulBaseline): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulBaselineRequiredShiftCodeRule(var message: String) : BackhaulBaselineRule {
    override fun check(item: BackhaulBaseline): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulBaselineCreatedAtRangeRule(var min: Int, var max: Int) : BackhaulBaselineRule {
    override fun check(item: BackhaulBaseline): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class BackhaulBaselineCodeLengthRule(var maxLength: Int) : BackhaulBaselineRule {
    override fun check(item: BackhaulBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulBaselineCodePrefixRule(var prefix: String) : BackhaulBaselineRule {
    override fun check(item: BackhaulBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulBaselineRequiredCodeRule(var message: String) : BackhaulBaselineRule {
    override fun check(item: BackhaulBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul baseline. */
data class BackhaulBaselineValidator(var rules: MutableList<BackhaulBaselineRule>) {
    fun validate(item: BackhaulBaseline): MutableList<String> {
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

    fun requireValid(item: BackhaulBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulBaselineValidator(): BackhaulBaselineValidator {
    return BackhaulBaselineValidator(
        mutableListOf<BackhaulBaselineRule>(
            BackhaulBaselineRequiredReviewerRule("backhaul baseline needs a value"),
            BackhaulBaselineRequiredShiftCodeRule("backhaul baseline needs this field"),
            BackhaulBaselineCreatedAtRangeRule(64, 64),
            BackhaulBaselineCodeLengthRule(16),
            BackhaulBaselineCodePrefixRule("OPS"),
            BackhaulBaselineRequiredCodeRule("backhaul baseline needs a value"),
        ),
    )
}
