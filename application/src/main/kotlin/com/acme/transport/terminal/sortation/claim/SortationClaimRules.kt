package com.acme.transport.terminal.sortation.claim

/** A rule a sortation claim must pass. Check returns its problems, or none. */
interface SortationClaimRule {
    fun check(item: SortationClaim): MutableList<String>
}

data class SortationClaimGraceDaysRangeRule(var min: Int, var max: Int) : SortationClaimRule {
    override fun check(item: SortationClaim): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SortationClaimMaximumRangeRule(var min: Double, var max: Double) : SortationClaimRule {
    override fun check(item: SortationClaim): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SortationClaimDurationMinutesRangeRule(var min: Int, var max: Int) : SortationClaimRule {
    override fun check(item: SortationClaim): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class SortationClaimRevisionRangeRule(var min: Int, var max: Int) : SortationClaimRule {
    override fun check(item: SortationClaim): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation claim. */
data class SortationClaimValidator(var rules: MutableList<SortationClaimRule>) {
    fun validate(item: SortationClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SortationClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationClaimValidator(): SortationClaimValidator {
    return SortationClaimValidator(
        mutableListOf<SortationClaimRule>(
            SortationClaimGraceDaysRangeRule(80, 1000),
            SortationClaimMaximumRangeRule(7.9, 3099.5),
            SortationClaimDurationMinutesRangeRule(80, 3600),
            SortationClaimRevisionRangeRule(32, 255),
        ),
    )
}
