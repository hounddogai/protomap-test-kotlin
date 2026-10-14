package com.acme.transport.linehaul.trailer.preference

/** A rule a trailer assignment must pass. Check returns its problems, or none. */
interface TrailerAssignmentRule {
    fun check(item: TrailerAssignment): MutableList<String>
}

data class TrailerAssignmentFuelLitersRangeRule(var min: Double, var max: Double) : TrailerAssignmentRule {
    override fun check(item: TrailerAssignment): MutableList<String> {
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

data class TrailerAssignmentBudgetRangeRule(var min: Double, var max: Double) : TrailerAssignmentRule {
    override fun check(item: TrailerAssignment): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerAssignmentStartsAtRangeRule(var min: Int, var max: Int) : TrailerAssignmentRule {
    override fun check(item: TrailerAssignment): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class TrailerAssignmentCodePrefixRule(var prefix: String) : TrailerAssignmentRule {
    override fun check(item: TrailerAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerAssignmentRequiredTimezoneRule(var message: String) : TrailerAssignmentRule {
    override fun check(item: TrailerAssignment): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer assignment. */
data class TrailerAssignmentValidator(var rules: MutableList<TrailerAssignmentRule>) {
    fun validate(item: TrailerAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerAssignmentValidator(): TrailerAssignmentValidator {
    return TrailerAssignmentValidator(
        mutableListOf<TrailerAssignmentRule>(
            TrailerAssignmentFuelLitersRangeRule(8.2, 4918.1),
            TrailerAssignmentBudgetRangeRule(3.7, 2209.4),
            TrailerAssignmentStartsAtRangeRule(120, 1000),
            TrailerAssignmentCodePrefixRule("OPS"),
            TrailerAssignmentRequiredTimezoneRule("trailer assignment needs this field"),
        ),
    )
}
