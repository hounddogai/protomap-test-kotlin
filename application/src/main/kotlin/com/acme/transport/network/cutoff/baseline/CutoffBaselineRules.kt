package com.acme.transport.network.cutoff.baseline

/** A rule a cutoff baseline must pass. Check returns its problems, or none. */
interface CutoffBaselineRule {
    fun check(item: CutoffBaseline): MutableList<String>
}

data class CutoffBaselineRequiredCodeRule(var message: String) : CutoffBaselineRule {
    override fun check(item: CutoffBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffBaselineCodePrefixRule(var prefix: String) : CutoffBaselineRule {
    override fun check(item: CutoffBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CutoffBaselineLabelLengthRule(var maxLength: Int) : CutoffBaselineRule {
    override fun check(item: CutoffBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffBaselineThresholdRangeRule(var min: Double, var max: Double) : CutoffBaselineRule {
    override fun check(item: CutoffBaseline): MutableList<String> {
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

/** Runs every rule of a cutoff baseline. */
data class CutoffBaselineValidator(var rules: MutableList<CutoffBaselineRule>) {
    fun validate(item: CutoffBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CutoffBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffBaselineValidator(): CutoffBaselineValidator {
    return CutoffBaselineValidator(
        mutableListOf<CutoffBaselineRule>(
            CutoffBaselineRequiredCodeRule("cutoff baseline needs a value"),
            CutoffBaselineCodePrefixRule("ACM"),
            CutoffBaselineLabelLengthRule(255),
            CutoffBaselineThresholdRangeRule(2.5, 1918.3),
        ),
    )
}
