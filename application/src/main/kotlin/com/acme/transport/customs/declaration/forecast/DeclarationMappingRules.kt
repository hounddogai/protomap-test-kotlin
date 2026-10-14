package com.acme.transport.customs.declaration.forecast

/** A rule a declaration mapping must pass. Check returns its problems, or none. */
interface DeclarationMappingRule {
    fun check(item: DeclarationMapping): MutableList<String>
}

data class DeclarationMappingExternalRefLengthRule(var maxLength: Int) : DeclarationMappingRule {
    override fun check(item: DeclarationMapping): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationMappingThresholdRangeRule(var min: Double, var max: Double) : DeclarationMappingRule {
    override fun check(item: DeclarationMapping): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class DeclarationMappingExpiresAtRangeRule(var min: Int, var max: Int) : DeclarationMappingRule {
    override fun check(item: DeclarationMapping): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a declaration mapping. */
data class DeclarationMappingValidator(var rules: MutableList<DeclarationMappingRule>) {
    fun validate(item: DeclarationMapping): MutableList<String> {
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

    fun requireValid(item: DeclarationMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationMappingValidator(): DeclarationMappingValidator {
    return DeclarationMappingValidator(
        mutableListOf<DeclarationMappingRule>(
            DeclarationMappingExternalRefLengthRule(32),
            DeclarationMappingThresholdRangeRule(8.7, 529.1),
            DeclarationMappingExpiresAtRangeRule(80, 86400),
        ),
    )
}
