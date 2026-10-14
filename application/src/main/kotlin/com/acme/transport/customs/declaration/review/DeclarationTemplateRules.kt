package com.acme.transport.customs.declaration.review

/** A rule a declaration template must pass. Check returns its problems, or none. */
interface DeclarationTemplateRule {
    fun check(item: DeclarationTemplate): MutableList<String>
}

data class DeclarationTemplateRequiredSourceRule(var message: String) : DeclarationTemplateRule {
    override fun check(item: DeclarationTemplate): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationTemplateCodeLengthRule(var maxLength: Int) : DeclarationTemplateRule {
    override fun check(item: DeclarationTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationTemplateRequiredCodeRule(var message: String) : DeclarationTemplateRule {
    override fun check(item: DeclarationTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationTemplateRateRangeRule(var min: Double, var max: Double) : DeclarationTemplateRule {
    override fun check(item: DeclarationTemplate): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationTemplateRevisionRangeRule(var min: Int, var max: Int) : DeclarationTemplateRule {
    override fun check(item: DeclarationTemplate): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationTemplateActiveGuardRule(var limit: Int) : DeclarationTemplateRule {
    override fun check(item: DeclarationTemplate): MutableList<String> {
        if (item.active && item.batchSize > limit) {
            return mutableListOf<String>("batch size ${item.batchSize} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration template. */
data class DeclarationTemplateValidator(var rules: MutableList<DeclarationTemplateRule>) {
    fun validate(item: DeclarationTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DeclarationTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationTemplateValidator(): DeclarationTemplateValidator {
    return DeclarationTemplateValidator(
        mutableListOf<DeclarationTemplateRule>(
            DeclarationTemplateRequiredSourceRule("declaration template needs an entry"),
            DeclarationTemplateCodeLengthRule(1000),
            DeclarationTemplateRequiredCodeRule("declaration template needs a value"),
            DeclarationTemplateRateRangeRule(5.4, 4671.9),
            DeclarationTemplateRevisionRangeRule(16, 80),
            DeclarationTemplateActiveGuardRule(1000),
        ),
    )
}
