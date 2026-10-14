package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.kit.text.normalize

/** A rule a trailer yard group must pass. Check returns its problems, or none. */
interface TrailerYardGroupRule {
    fun check(item: TrailerYardGroup): MutableList<String>
}

data class TrailerYardGroupRequiredNameRule(var message: String) : TrailerYardGroupRule {
    override fun check(item: TrailerYardGroup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardGroupNameLengthRule(var maxLength: Int) : TrailerYardGroupRule {
    override fun check(item: TrailerYardGroup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardGroupAllowedNameRule(var allowed: MutableList<String>) : TrailerYardGroupRule {
    override fun check(item: TrailerYardGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardGroupRequiredReviewerRule(var message: String) : TrailerYardGroupRule {
    override fun check(item: TrailerYardGroup): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardGroupReviewerLengthRule(var maxLength: Int) : TrailerYardGroupRule {
    override fun check(item: TrailerYardGroup): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardGroupGraceDaysRangeRule(var min: Int, var max: Int) : TrailerYardGroupRule {
    override fun check(item: TrailerYardGroup): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard group. */
data class TrailerYardGroupValidator(var rules: MutableList<TrailerYardGroupRule>) {
    fun validate(item: TrailerYardGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardGroupValidator(): TrailerYardGroupValidator {
    return TrailerYardGroupValidator(
        mutableListOf<TrailerYardGroupRule>(
            TrailerYardGroupRequiredNameRule("trailer yard group needs an entry"),
            TrailerYardGroupNameLengthRule(32),
            TrailerYardGroupAllowedNameRule(mutableListOf<String>()),
            TrailerYardGroupRequiredReviewerRule("trailer yard group needs an entry"),
            TrailerYardGroupReviewerLengthRule(120),
            TrailerYardGroupGraceDaysRangeRule(80, 255),
        ),
    )
}
