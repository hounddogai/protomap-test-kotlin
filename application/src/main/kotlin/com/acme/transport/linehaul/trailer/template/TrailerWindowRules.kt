package com.acme.transport.linehaul.trailer.template

import com.acme.transport.kit.text.normalize

/** A rule a trailer window must pass. Check returns its problems, or none. */
interface TrailerWindowRule {
    fun check(item: TrailerWindow): MutableList<String>
}

data class TrailerWindowAllowedExternalRefRule(var allowed: MutableList<String>) : TrailerWindowRule {
    override fun check(item: TrailerWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class TrailerWindowFuelLitersRangeRule(var min: Double, var max: Double) : TrailerWindowRule {
    override fun check(item: TrailerWindow): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class TrailerWindowUpdatedAtRangeRule(var min: Int, var max: Int) : TrailerWindowRule {
    override fun check(item: TrailerWindow): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class TrailerWindowTitleLengthRule(var maxLength: Int) : TrailerWindowRule {
    override fun check(item: TrailerWindow): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerWindowRequiredTitleRule(var message: String) : TrailerWindowRule {
    override fun check(item: TrailerWindow): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerWindowCodeLengthRule(var maxLength: Int) : TrailerWindowRule {
    override fun check(item: TrailerWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer window. */
data class TrailerWindowValidator(var rules: MutableList<TrailerWindowRule>) {
    fun validate(item: TrailerWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerWindowValidator(): TrailerWindowValidator {
    return TrailerWindowValidator(
        mutableListOf<TrailerWindowRule>(
            TrailerWindowAllowedExternalRefRule(mutableListOf<String>()),
            TrailerWindowFuelLitersRangeRule(9.1, 1706.2),
            TrailerWindowUpdatedAtRangeRule(120, 3600),
            TrailerWindowTitleLengthRule(86400),
            TrailerWindowRequiredTitleRule("trailer window needs an entry"),
            TrailerWindowCodeLengthRule(255),
        ),
    )
}
