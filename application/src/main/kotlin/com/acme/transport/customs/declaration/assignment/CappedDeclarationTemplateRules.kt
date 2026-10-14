package com.acme.transport.customs.declaration.assignment

/** A rule a capped declaration template must pass. Check returns its problems, or none. */
interface CappedDeclarationTemplateRule {
    fun check(item: CappedDeclarationTemplate): MutableList<String>
}

data class CappedDeclarationTemplateRequiredCodeRule(var message: String) : CappedDeclarationTemplateRule {
    override fun check(item: CappedDeclarationTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDeclarationTemplateReviewerLengthRule(var maxLength: Int) : CappedDeclarationTemplateRule {
    override fun check(item: CappedDeclarationTemplate): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDeclarationTemplatePriorityRangeRule(
    var min: Int,
    var max: Int,
) : CappedDeclarationTemplateRule {
    override fun check(item: CappedDeclarationTemplate): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class CappedDeclarationTemplateLimitRangeRule(var min: Int, var max: Int) : CappedDeclarationTemplateRule {
    override fun check(item: CappedDeclarationTemplate): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class CappedDeclarationTemplateTitleLengthRule(var maxLength: Int) : CappedDeclarationTemplateRule {
    override fun check(item: CappedDeclarationTemplate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped declaration template. */
data class CappedDeclarationTemplateValidator(var rules: MutableList<CappedDeclarationTemplateRule>) {
    fun validate(item: CappedDeclarationTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedDeclarationTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDeclarationTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDeclarationTemplateValidator(): CappedDeclarationTemplateValidator {
    return CappedDeclarationTemplateValidator(
        mutableListOf<CappedDeclarationTemplateRule>(
            CappedDeclarationTemplateRequiredCodeRule("capped declaration template needs this field"),
            CappedDeclarationTemplateReviewerLengthRule(32),
            CappedDeclarationTemplatePriorityRangeRule(32, 255),
            CappedDeclarationTemplateLimitRangeRule(16, 32),
            CappedDeclarationTemplateTitleLengthRule(32),
        ),
    )
}
