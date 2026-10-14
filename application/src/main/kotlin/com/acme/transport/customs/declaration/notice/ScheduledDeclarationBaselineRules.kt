package com.acme.transport.customs.declaration.notice

/** A rule a scheduled declaration baseline must pass. Check returns its problems, or none. */
interface ScheduledDeclarationBaselineRule {
    fun check(item: ScheduledDeclarationBaseline): MutableList<String>
}

data class ScheduledDeclarationBaselineRequiredContactEmailRule(
    var message: String,
) : ScheduledDeclarationBaselineRule {
    override fun check(item: ScheduledDeclarationBaseline): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDeclarationBaselineCodePrefixRule(var prefix: String) : ScheduledDeclarationBaselineRule {
    override fun check(item: ScheduledDeclarationBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDeclarationBaselineCategoryLengthRule(
    var maxLength: Int,
) : ScheduledDeclarationBaselineRule {
    override fun check(item: ScheduledDeclarationBaseline): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDeclarationBaselineRequiredCodeRule(
    var message: String,
) : ScheduledDeclarationBaselineRule {
    override fun check(item: ScheduledDeclarationBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled declaration baseline. */
data class ScheduledDeclarationBaselineValidator(var rules: MutableList<ScheduledDeclarationBaselineRule>) {
    fun validate(item: ScheduledDeclarationBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledDeclarationBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDeclarationBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDeclarationBaselineValidator(): ScheduledDeclarationBaselineValidator {
    return ScheduledDeclarationBaselineValidator(
        mutableListOf<ScheduledDeclarationBaselineRule>(
            ScheduledDeclarationBaselineRequiredContactEmailRule("scheduled declaration baseline needs a value"),
            ScheduledDeclarationBaselineCodePrefixRule("Q"),
            ScheduledDeclarationBaselineCategoryLengthRule(120),
            ScheduledDeclarationBaselineRequiredCodeRule("scheduled declaration baseline needs a value"),
        ),
    )
}
