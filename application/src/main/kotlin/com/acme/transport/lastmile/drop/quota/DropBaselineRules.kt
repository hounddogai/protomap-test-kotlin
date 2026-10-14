package com.acme.transport.lastmile.drop.quota

import com.acme.transport.kit.text.normalize

/** A rule a drop baseline must pass. Check returns its problems, or none. */
interface DropBaselineRule {
    fun check(item: DropBaseline): MutableList<String>
}

data class DropBaselineAllowedNotesRule(var allowed: MutableList<String>) : DropBaselineRule {
    override fun check(item: DropBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class DropBaselineCodeLengthRule(var maxLength: Int) : DropBaselineRule {
    override fun check(item: DropBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropBaselineLabelLengthRule(var maxLength: Int) : DropBaselineRule {
    override fun check(item: DropBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropBaselineRequiredLabelRule(var message: String) : DropBaselineRule {
    override fun check(item: DropBaseline): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropBaselineRateRangeRule(var min: Double, var max: Double) : DropBaselineRule {
    override fun check(item: DropBaseline): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class DropBaselineRequiredTitleRule(var message: String) : DropBaselineRule {
    override fun check(item: DropBaseline): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop baseline. */
data class DropBaselineValidator(var rules: MutableList<DropBaselineRule>) {
    fun validate(item: DropBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DropBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultDropBaselineValidator(): DropBaselineValidator {
    return DropBaselineValidator(
        mutableListOf<DropBaselineRule>(
            DropBaselineAllowedNotesRule(mutableListOf<String>()),
            DropBaselineCodeLengthRule(255),
            DropBaselineLabelLengthRule(1000),
            DropBaselineRequiredLabelRule("drop baseline needs this field"),
            DropBaselineRateRangeRule(4.6, 230.8),
            DropBaselineRequiredTitleRule("drop baseline needs an entry"),
        ),
    )
}
