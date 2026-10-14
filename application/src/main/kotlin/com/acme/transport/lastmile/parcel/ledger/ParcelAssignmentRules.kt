package com.acme.transport.lastmile.parcel.ledger

/** A rule a parcel assignment must pass. Check returns its problems, or none. */
interface ParcelAssignmentRule {
    fun check(item: ParcelAssignment): MutableList<String>
}

data class ParcelAssignmentDurationMinutesRangeRule(var min: Int, var max: Int) : ParcelAssignmentRule {
    override fun check(item: ParcelAssignment): MutableList<String> {
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

data class ParcelAssignmentPriorityRangeRule(var min: Int, var max: Int) : ParcelAssignmentRule {
    override fun check(item: ParcelAssignment): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ParcelAssignmentTitleLengthRule(var maxLength: Int) : ParcelAssignmentRule {
    override fun check(item: ParcelAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelAssignmentRequiredTitleRule(var message: String) : ParcelAssignmentRule {
    override fun check(item: ParcelAssignment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel assignment. */
data class ParcelAssignmentValidator(var rules: MutableList<ParcelAssignmentRule>) {
    fun validate(item: ParcelAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelAssignmentValidator(): ParcelAssignmentValidator {
    return ParcelAssignmentValidator(
        mutableListOf<ParcelAssignmentRule>(
            ParcelAssignmentDurationMinutesRangeRule(32, 1000),
            ParcelAssignmentPriorityRangeRule(255, 3600),
            ParcelAssignmentTitleLengthRule(86400),
            ParcelAssignmentRequiredTitleRule("parcel assignment needs a value"),
        ),
    )
}
