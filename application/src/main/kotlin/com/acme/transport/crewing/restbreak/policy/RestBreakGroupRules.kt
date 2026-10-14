package com.acme.transport.crewing.restbreak.policy

/** A rule a rest break group must pass. Check returns its problems, or none. */
interface RestBreakGroupRule {
    fun check(item: RestBreakGroup): MutableList<String>
}

data class RestBreakGroupSequenceRangeRule(var min: Int, var max: Int) : RestBreakGroupRule {
    override fun check(item: RestBreakGroup): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class RestBreakGroupRequiredCodeRule(var message: String) : RestBreakGroupRule {
    override fun check(item: RestBreakGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakGroupCodeLengthRule(var maxLength: Int) : RestBreakGroupRule {
    override fun check(item: RestBreakGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakGroupUpdatedAtRangeRule(var min: Int, var max: Int) : RestBreakGroupRule {
    override fun check(item: RestBreakGroup): MutableList<String> {
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

data class RestBreakGroupLabelLengthRule(var maxLength: Int) : RestBreakGroupRule {
    override fun check(item: RestBreakGroup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break group. */
data class RestBreakGroupValidator(var rules: MutableList<RestBreakGroupRule>) {
    fun validate(item: RestBreakGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakGroupValidator(): RestBreakGroupValidator {
    return RestBreakGroupValidator(
        mutableListOf<RestBreakGroupRule>(
            RestBreakGroupSequenceRangeRule(32, 255),
            RestBreakGroupRequiredCodeRule("rest break group needs this field"),
            RestBreakGroupCodeLengthRule(86400),
            RestBreakGroupUpdatedAtRangeRule(120, 3600),
            RestBreakGroupLabelLengthRule(3600),
        ),
    )
}
