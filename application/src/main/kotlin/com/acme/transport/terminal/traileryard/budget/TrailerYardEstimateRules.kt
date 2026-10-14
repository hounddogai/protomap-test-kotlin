package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.text.normalize

/** A rule a trailer yard estimate must pass. Check returns its problems, or none. */
interface TrailerYardEstimateRule {
    fun check(item: TrailerYardEstimate): MutableList<String>
}

data class TrailerYardEstimateWeightRangeRule(var min: Double, var max: Double) : TrailerYardEstimateRule {
    override fun check(item: TrailerYardEstimate): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardEstimateAllowedChecksumRule(var allowed: MutableList<String>) : TrailerYardEstimateRule {
    override fun check(item: TrailerYardEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardEstimateCodePrefixRule(var prefix: String) : TrailerYardEstimateRule {
    override fun check(item: TrailerYardEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardEstimateRatioRangeRule(var min: Double, var max: Double) : TrailerYardEstimateRule {
    override fun check(item: TrailerYardEstimate): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class TrailerYardEstimateRequiredShiftCodeRule(var message: String) : TrailerYardEstimateRule {
    override fun check(item: TrailerYardEstimate): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard estimate. */
data class TrailerYardEstimateValidator(var rules: MutableList<TrailerYardEstimateRule>) {
    fun validate(item: TrailerYardEstimate): MutableList<String> {
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

    fun requireValid(item: TrailerYardEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardEstimateValidator(): TrailerYardEstimateValidator {
    return TrailerYardEstimateValidator(
        mutableListOf<TrailerYardEstimateRule>(
            TrailerYardEstimateWeightRangeRule(4.2, 4226.0),
            TrailerYardEstimateAllowedChecksumRule(mutableListOf<String>()),
            TrailerYardEstimateCodePrefixRule("OPS"),
            TrailerYardEstimateRatioRangeRule(2.2, 845.6),
            TrailerYardEstimateRequiredShiftCodeRule("trailer yard estimate needs this field"),
        ),
    )
}
