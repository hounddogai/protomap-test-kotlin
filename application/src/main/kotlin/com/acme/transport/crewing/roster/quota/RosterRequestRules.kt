package com.acme.transport.crewing.roster.quota

/** A rule a roster request must pass. Check returns its problems, or none. */
interface RosterRequestRule {
    fun check(item: RosterRequest): MutableList<String>
}

data class RosterRequestLabelLengthRule(var maxLength: Int) : RosterRequestRule {
    override fun check(item: RosterRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterRequestContactEmailLengthRule(var maxLength: Int) : RosterRequestRule {
    override fun check(item: RosterRequest): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterRequestCodeLengthRule(var maxLength: Int) : RosterRequestRule {
    override fun check(item: RosterRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterRequestCeilingRangeRule(var min: Double, var max: Double) : RosterRequestRule {
    override fun check(item: RosterRequest): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class RosterRequestCodePrefixRule(var prefix: String) : RosterRequestRule {
    override fun check(item: RosterRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster request. */
data class RosterRequestValidator(var rules: MutableList<RosterRequestRule>) {
    fun validate(item: RosterRequest): MutableList<String> {
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

    fun requireValid(item: RosterRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterRequestValidator(): RosterRequestValidator {
    return RosterRequestValidator(
        mutableListOf<RosterRequestRule>(
            RosterRequestLabelLengthRule(1000),
            RosterRequestContactEmailLengthRule(1000),
            RosterRequestCodeLengthRule(255),
            RosterRequestCeilingRangeRule(7.1, 3898.2),
            RosterRequestCodePrefixRule("X"),
        ),
    )
}
