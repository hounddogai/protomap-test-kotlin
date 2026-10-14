package com.acme.transport.customs.declaration.declarationforecast

/** A rule a declaration claim must pass. Check returns its problems, or none. */
interface DeclarationClaimRule {
    fun check(item: DeclarationClaim): MutableList<String>
}

data class DeclarationClaimVersionRangeRule(var min: Int, var max: Int) : DeclarationClaimRule {
    override fun check(item: DeclarationClaim): MutableList<String> {
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

data class DeclarationClaimRequiredCodeRule(var message: String) : DeclarationClaimRule {
    override fun check(item: DeclarationClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationClaimLocaleLengthRule(var maxLength: Int) : DeclarationClaimRule {
    override fun check(item: DeclarationClaim): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration claim. */
data class DeclarationClaimValidator(var rules: MutableList<DeclarationClaimRule>) {
    fun validate(item: DeclarationClaim): MutableList<String> {
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

    fun requireValid(item: DeclarationClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationClaimValidator(): DeclarationClaimValidator {
    return DeclarationClaimValidator(
        mutableListOf<DeclarationClaimRule>(
            DeclarationClaimVersionRangeRule(16, 1000),
            DeclarationClaimRequiredCodeRule("declaration claim needs this field"),
            DeclarationClaimLocaleLengthRule(120),
        ),
    )
}
