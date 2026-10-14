package com.acme.transport.crewing.roster.exception

/** A rule a roster mapping must pass. Check returns its problems, or none. */
interface RosterMappingRule {
    fun check(item: RosterMapping): MutableList<String>
}

data class RosterMappingLabelLengthRule(var maxLength: Int) : RosterMappingRule {
    override fun check(item: RosterMapping): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterMappingRequiredRouteCodeRule(var message: String) : RosterMappingRule {
    override fun check(item: RosterMapping): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterMappingRequiredExternalRefRule(var message: String) : RosterMappingRule {
    override fun check(item: RosterMapping): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterMappingPalletsRangeRule(var min: Int, var max: Int) : RosterMappingRule {
    override fun check(item: RosterMapping): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a roster mapping. */
data class RosterMappingValidator(var rules: MutableList<RosterMappingRule>) {
    fun validate(item: RosterMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterMappingValidator(): RosterMappingValidator {
    return RosterMappingValidator(
        mutableListOf<RosterMappingRule>(
            RosterMappingLabelLengthRule(3600),
            RosterMappingRequiredRouteCodeRule("roster mapping needs this field"),
            RosterMappingRequiredExternalRefRule("roster mapping needs a value"),
            RosterMappingPalletsRangeRule(16, 120),
        ),
    )
}
