package com.acme.transport.customs.declaration.threshold

/** A rule a declaration record must pass. Check returns its problems, or none. */
interface DeclarationRecordRule {
    fun check(item: DeclarationRecord): MutableList<String>
}

data class DeclarationRecordCodeLengthRule(var maxLength: Int) : DeclarationRecordRule {
    override fun check(item: DeclarationRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationRecordCodePrefixRule(var prefix: String) : DeclarationRecordRule {
    override fun check(item: DeclarationRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationRecordRetriesRangeRule(var min: Int, var max: Int) : DeclarationRecordRule {
    override fun check(item: DeclarationRecord): MutableList<String> {
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

/** Runs every rule of a declaration record. */
data class DeclarationRecordValidator(var rules: MutableList<DeclarationRecordRule>) {
    fun validate(item: DeclarationRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DeclarationRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationRecordValidator(): DeclarationRecordValidator {
    return DeclarationRecordValidator(
        mutableListOf<DeclarationRecordRule>(
            DeclarationRecordCodeLengthRule(32),
            DeclarationRecordCodePrefixRule("ACM"),
            DeclarationRecordRetriesRangeRule(64, 1000),
        ),
    )
}
