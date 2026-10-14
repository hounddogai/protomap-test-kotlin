package com.acme.transport.fleetops.tire.target

import com.acme.transport.kit.text.normalize

/** A rule a pending tire target must pass. Check returns its problems, or none. */
interface PendingTireTargetRule {
    fun check(item: PendingTireTarget): MutableList<String>
}

data class PendingTireTargetCodePrefixRule(var prefix: String) : PendingTireTargetRule {
    override fun check(item: PendingTireTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingTireTargetBudgetRangeRule(var min: Double, var max: Double) : PendingTireTargetRule {
    override fun check(item: PendingTireTarget): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingTireTargetAllowedDescriptionRule(var allowed: MutableList<String>) : PendingTireTargetRule {
    override fun check(item: PendingTireTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class PendingTireTargetTrailerIdLengthRule(var maxLength: Int) : PendingTireTargetRule {
    override fun check(item: PendingTireTarget): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending tire target. */
data class PendingTireTargetValidator(var rules: MutableList<PendingTireTargetRule>) {
    fun validate(item: PendingTireTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingTireTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTireTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTireTargetValidator(): PendingTireTargetValidator {
    return PendingTireTargetValidator(
        mutableListOf<PendingTireTargetRule>(
            PendingTireTargetCodePrefixRule("OPS"),
            PendingTireTargetBudgetRangeRule(2.5, 3527.7),
            PendingTireTargetAllowedDescriptionRule(mutableListOf<String>("tiered")),
            PendingTireTargetTrailerIdLengthRule(80),
        ),
    )
}
