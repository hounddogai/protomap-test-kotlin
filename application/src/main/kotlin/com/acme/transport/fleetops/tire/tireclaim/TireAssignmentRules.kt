package com.acme.transport.fleetops.tire.tireclaim

/** A rule a tire assignment must pass. Check returns its problems, or none. */
interface TireAssignmentRule {
    fun check(item: TireAssignment): MutableList<String>
}

data class TireAssignmentCodeLengthRule(var maxLength: Int) : TireAssignmentRule {
    override fun check(item: TireAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireAssignmentDurationMinutesRangeRule(var min: Int, var max: Int) : TireAssignmentRule {
    override fun check(item: TireAssignment): MutableList<String> {
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

data class TireAssignmentCodePrefixRule(var prefix: String) : TireAssignmentRule {
    override fun check(item: TireAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TireAssignmentRequiredTitleRule(var message: String) : TireAssignmentRule {
    override fun check(item: TireAssignment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireAssignmentRegionLengthRule(var maxLength: Int) : TireAssignmentRule {
    override fun check(item: TireAssignment): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireAssignmentRequiredCodeRule(var message: String) : TireAssignmentRule {
    override fun check(item: TireAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire assignment. */
data class TireAssignmentValidator(var rules: MutableList<TireAssignmentRule>) {
    fun validate(item: TireAssignment): MutableList<String> {
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

    fun requireValid(item: TireAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultTireAssignmentValidator(): TireAssignmentValidator {
    return TireAssignmentValidator(
        mutableListOf<TireAssignmentRule>(
            TireAssignmentCodeLengthRule(64),
            TireAssignmentDurationMinutesRangeRule(120, 1000),
            TireAssignmentCodePrefixRule("Q"),
            TireAssignmentRequiredTitleRule("tire assignment needs a value"),
            TireAssignmentRegionLengthRule(80),
            TireAssignmentRequiredCodeRule("tire assignment needs a value"),
        ),
    )
}
