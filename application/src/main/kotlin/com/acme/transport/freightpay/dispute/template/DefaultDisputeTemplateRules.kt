package com.acme.transport.freightpay.dispute.template

/** A rule a default dispute template must pass. Check returns its problems, or none. */
interface DefaultDisputeTemplateRule {
    fun check(item: DefaultDisputeTemplate): MutableList<String>
}

data class DefaultDisputeTemplateOwnerIdLengthRule(var maxLength: Int) : DefaultDisputeTemplateRule {
    override fun check(item: DefaultDisputeTemplate): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDisputeTemplateCeilingRangeRule(
    var min: Double,
    var max: Double,
) : DefaultDisputeTemplateRule {
    override fun check(item: DefaultDisputeTemplate): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDisputeTemplateRequiredOwnerIdRule(var message: String) : DefaultDisputeTemplateRule {
    override fun check(item: DefaultDisputeTemplate): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default dispute template. */
data class DefaultDisputeTemplateValidator(var rules: MutableList<DefaultDisputeTemplateRule>) {
    fun validate(item: DefaultDisputeTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultDisputeTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDisputeTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDisputeTemplateValidator(): DefaultDisputeTemplateValidator {
    return DefaultDisputeTemplateValidator(
        mutableListOf<DefaultDisputeTemplateRule>(
            DefaultDisputeTemplateOwnerIdLengthRule(16),
            DefaultDisputeTemplateCeilingRangeRule(7.6, 674.1),
            DefaultDisputeTemplateRequiredOwnerIdRule("default dispute template needs an entry"),
        ),
    )
}
