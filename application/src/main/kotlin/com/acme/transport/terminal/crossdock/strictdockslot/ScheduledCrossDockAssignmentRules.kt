package com.acme.transport.terminal.crossdock.strictdockslot

/** A rule a scheduled cross dock assignment must pass. Check returns its problems, or none. */
interface ScheduledCrossDockAssignmentRule {
    fun check(item: ScheduledCrossDockAssignment): MutableList<String>
}

data class ScheduledCrossDockAssignmentRequiredCodeRule(
    var message: String,
) : ScheduledCrossDockAssignmentRule {
    override fun check(item: ScheduledCrossDockAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCrossDockAssignmentTitleLengthRule(var maxLength: Int) : ScheduledCrossDockAssignmentRule {
    override fun check(item: ScheduledCrossDockAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCrossDockAssignmentCodePrefixRule(var prefix: String) : ScheduledCrossDockAssignmentRule {
    override fun check(item: ScheduledCrossDockAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCrossDockAssignmentVersionRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledCrossDockAssignmentRule {
    override fun check(item: ScheduledCrossDockAssignment): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled cross dock assignment. */
data class ScheduledCrossDockAssignmentValidator(var rules: MutableList<ScheduledCrossDockAssignmentRule>) {
    fun validate(item: ScheduledCrossDockAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledCrossDockAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledCrossDockAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledCrossDockAssignmentValidator(): ScheduledCrossDockAssignmentValidator {
    return ScheduledCrossDockAssignmentValidator(
        mutableListOf<ScheduledCrossDockAssignmentRule>(
            ScheduledCrossDockAssignmentRequiredCodeRule("scheduled cross dock assignment needs this field"),
            ScheduledCrossDockAssignmentTitleLengthRule(86400),
            ScheduledCrossDockAssignmentCodePrefixRule("ACM"),
            ScheduledCrossDockAssignmentVersionRangeRule(255, 255),
        ),
    )
}
