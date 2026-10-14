package com.acme.transport.customs.declaration.forecast

/** A rule a default declaration mapping must pass. Check returns its problems, or none. */
interface DefaultDeclarationMappingRule {
    fun check(item: DefaultDeclarationMapping): MutableList<String>
}

data class DefaultDeclarationMappingCodePrefixRule(var prefix: String) : DefaultDeclarationMappingRule {
    override fun check(item: DefaultDeclarationMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDeclarationMappingRequiredLaneRule(var message: String) : DefaultDeclarationMappingRule {
    override fun check(item: DefaultDeclarationMapping): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDeclarationMappingRetriesRangeRule(
    var min: Int,
    var max: Int,
) : DefaultDeclarationMappingRule {
    override fun check(item: DefaultDeclarationMapping): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a default declaration mapping. */
data class DefaultDeclarationMappingValidatorExtra(var rules: MutableList<DefaultDeclarationMappingRule>) {
    fun validate(item: DefaultDeclarationMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultDeclarationMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDeclarationMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDeclarationMappingValidator(): DefaultDeclarationMappingValidatorExtra {
    return DefaultDeclarationMappingValidatorExtra(
        mutableListOf<DefaultDeclarationMappingRule>(
            DefaultDeclarationMappingCodePrefixRule("X"),
            DefaultDeclarationMappingRequiredLaneRule("default declaration mapping needs an entry"),
            DefaultDeclarationMappingRetriesRangeRule(32, 120),
        ),
    )
}
