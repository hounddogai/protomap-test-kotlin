package com.acme.transport.customs.declaration.assignment

/** A rule a declaration policy must pass. Check returns its problems, or none. */
interface DeclarationPolicyRule {
    fun check(item: DeclarationPolicy): MutableList<String>
}

data class DeclarationPolicyVersionRangeRule(var min: Int, var max: Int) : DeclarationPolicyRule {
    override fun check(item: DeclarationPolicy): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationPolicyRequiredLabelRule(var message: String) : DeclarationPolicyRule {
    override fun check(item: DeclarationPolicy): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationPolicyCodeLengthRule(var maxLength: Int) : DeclarationPolicyRule {
    override fun check(item: DeclarationPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration policy. */
data class DeclarationPolicyValidator(var rules: MutableList<DeclarationPolicyRule>) {
    fun validate(item: DeclarationPolicy): MutableList<String> {
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

    fun requireValid(item: DeclarationPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationPolicyValidator(): DeclarationPolicyValidator {
    return DeclarationPolicyValidator(
        mutableListOf<DeclarationPolicyRule>(
            DeclarationPolicyVersionRangeRule(16, 255),
            DeclarationPolicyRequiredLabelRule("declaration policy needs this field"),
            DeclarationPolicyCodeLengthRule(16),
        ),
    )
}
