package com.acme.transport.terminal.traileryard.estimate

import com.acme.transport.kit.text.normalize

/** A rule a trailer yard adjustment must pass. Check returns its problems, or none. */
interface TrailerYardAdjustmentRule {
    fun check(item: TrailerYardAdjustment): MutableList<String>
}

data class TrailerYardAdjustmentThresholdRangeRule(
    var min: Double,
    var max: Double,
) : TrailerYardAdjustmentRule {
    override fun check(item: TrailerYardAdjustment): MutableList<String> {
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

data class TrailerYardAdjustmentAllowedCodeRule(var allowed: MutableList<String>) : TrailerYardAdjustmentRule {
    override fun check(item: TrailerYardAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardAdjustmentRequiredCodeRule(var message: String) : TrailerYardAdjustmentRule {
    override fun check(item: TrailerYardAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardAdjustmentCodePrefixRule(var prefix: String) : TrailerYardAdjustmentRule {
    override fun check(item: TrailerYardAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardAdjustmentRequiredNameRule(var message: String) : TrailerYardAdjustmentRule {
    override fun check(item: TrailerYardAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardAdjustmentCodeLengthRule(var maxLength: Int) : TrailerYardAdjustmentRule {
    override fun check(item: TrailerYardAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard adjustment. */
data class TrailerYardAdjustmentValidator(var rules: MutableList<TrailerYardAdjustmentRule>) {
    fun validate(item: TrailerYardAdjustment): MutableList<String> {
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

    fun requireValid(item: TrailerYardAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardAdjustmentValidator(): TrailerYardAdjustmentValidator {
    return TrailerYardAdjustmentValidator(
        mutableListOf<TrailerYardAdjustmentRule>(
            TrailerYardAdjustmentThresholdRangeRule(7.0, 3495.2),
            TrailerYardAdjustmentAllowedCodeRule(mutableListOf<String>("weighted", "pending")),
            TrailerYardAdjustmentRequiredCodeRule("trailer yard adjustment needs a value"),
            TrailerYardAdjustmentCodePrefixRule("REF"),
            TrailerYardAdjustmentRequiredNameRule("trailer yard adjustment needs an entry"),
            TrailerYardAdjustmentCodeLengthRule(1000),
        ),
    )
}
