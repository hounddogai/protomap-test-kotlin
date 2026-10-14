package com.acme.transport.crewing.roster.mapping

/** A rule a default roster mapping must pass. Check returns its problems, or none. */
interface DefaultRosterMappingRule {
    fun check(item: DefaultRosterMapping): MutableList<String>
}

data class DefaultRosterMappingOdometerRangeRule(var min: Int, var max: Int) : DefaultRosterMappingRule {
    override fun check(item: DefaultRosterMapping): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRosterMappingRequiredCodeRule(var message: String) : DefaultRosterMappingRule {
    override fun check(item: DefaultRosterMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRosterMappingLabelLengthRule(var maxLength: Int) : DefaultRosterMappingRule {
    override fun check(item: DefaultRosterMapping): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRosterMappingRequiredPhoneRule(var message: String) : DefaultRosterMappingRule {
    override fun check(item: DefaultRosterMapping): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default roster mapping. */
data class DefaultRosterMappingValidator(var rules: MutableList<DefaultRosterMappingRule>) {
    fun validate(item: DefaultRosterMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultRosterMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRosterMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRosterMappingValidator(): DefaultRosterMappingValidator {
    return DefaultRosterMappingValidator(
        mutableListOf<DefaultRosterMappingRule>(
            DefaultRosterMappingOdometerRangeRule(1000, 3600),
            DefaultRosterMappingRequiredCodeRule("default roster mapping needs this field"),
            DefaultRosterMappingLabelLengthRule(120),
            DefaultRosterMappingRequiredPhoneRule("default roster mapping needs a value"),
        ),
    )
}
