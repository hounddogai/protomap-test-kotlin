package com.acme.transport.customs.clearance.slot

/** A rule a default clearance target must pass. Check returns its problems, or none. */
interface DefaultClearanceTargetRule {
    fun check(item: DefaultClearanceTarget): MutableList<String>
}

data class DefaultClearanceTargetTrailerIdLengthRule(var maxLength: Int) : DefaultClearanceTargetRule {
    override fun check(item: DefaultClearanceTarget): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultClearanceTargetStartsAtRangeRule(var min: Int, var max: Int) : DefaultClearanceTargetRule {
    override fun check(item: DefaultClearanceTarget): MutableList<String> {
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

data class DefaultClearanceTargetCodeLengthRule(var maxLength: Int) : DefaultClearanceTargetRule {
    override fun check(item: DefaultClearanceTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultClearanceTargetPriorityRangeRule(var min: Int, var max: Int) : DefaultClearanceTargetRule {
    override fun check(item: DefaultClearanceTarget): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class DefaultClearanceTargetEtaMinutesRangeRule(var min: Int, var max: Int) : DefaultClearanceTargetRule {
    override fun check(item: DefaultClearanceTarget): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default clearance target. */
data class DefaultClearanceTargetValidator(var rules: MutableList<DefaultClearanceTargetRule>) {
    fun validate(item: DefaultClearanceTarget): MutableList<String> {
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

    fun requireValid(item: DefaultClearanceTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultClearanceTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultClearanceTargetValidator(): DefaultClearanceTargetValidator {
    return DefaultClearanceTargetValidator(
        mutableListOf<DefaultClearanceTargetRule>(
            DefaultClearanceTargetTrailerIdLengthRule(64),
            DefaultClearanceTargetStartsAtRangeRule(32, 1000),
            DefaultClearanceTargetCodeLengthRule(32),
            DefaultClearanceTargetPriorityRangeRule(32, 1000),
            DefaultClearanceTargetEtaMinutesRangeRule(64, 255),
        ),
    )
}
