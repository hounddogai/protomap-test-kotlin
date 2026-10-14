package com.acme.transport.customs.declaration.exception

/** A rule a declaration exception must pass. Check returns its problems, or none. */
interface DeclarationExceptionRule {
    fun check(item: DeclarationException): MutableList<String>
}

data class DeclarationExceptionRevisionRangeRule(var min: Int, var max: Int) : DeclarationExceptionRule {
    override fun check(item: DeclarationException): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationExceptionRatioRangeRule(var min: Double, var max: Double) : DeclarationExceptionRule {
    override fun check(item: DeclarationException): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationExceptionCodePrefixRule(var prefix: String) : DeclarationExceptionRule {
    override fun check(item: DeclarationException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration exception. */
data class DeclarationExceptionValidator(var rules: MutableList<DeclarationExceptionRule>) {
    fun validate(item: DeclarationException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DeclarationException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationException>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationExceptionValidator(): DeclarationExceptionValidator {
    return DeclarationExceptionValidator(
        mutableListOf<DeclarationExceptionRule>(
            DeclarationExceptionRevisionRangeRule(64, 86400),
            DeclarationExceptionRatioRangeRule(7.4, 3585.1),
            DeclarationExceptionCodePrefixRule("OPS"),
        ),
    )
}
