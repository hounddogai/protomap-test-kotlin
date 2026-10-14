package com.acme.transport.terminal.dwell.template

/** A rule a dwell template must pass. Check returns its problems, or none. */
interface DwellTemplateRule {
    fun check(item: DwellTemplate): MutableList<String>
}

data class DwellTemplateMinimumRangeRule(var min: Double, var max: Double) : DwellTemplateRule {
    override fun check(item: DwellTemplate): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class DwellTemplateExternalRefLengthRule(var maxLength: Int) : DwellTemplateRule {
    override fun check(item: DwellTemplate): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellTemplateRequiredExternalRefRule(var message: String) : DwellTemplateRule {
    override fun check(item: DwellTemplate): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellTemplateRequiredCategoryRule(var message: String) : DwellTemplateRule {
    override fun check(item: DwellTemplate): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellTemplateCodeLengthRule(var maxLength: Int) : DwellTemplateRule {
    override fun check(item: DwellTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell template. */
data class DwellTemplateValidator(var rules: MutableList<DwellTemplateRule>) {
    fun validate(item: DwellTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DwellTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellTemplateValidator(): DwellTemplateValidator {
    return DwellTemplateValidator(
        mutableListOf<DwellTemplateRule>(
            DwellTemplateMinimumRangeRule(5.9, 3734.4),
            DwellTemplateExternalRefLengthRule(255),
            DwellTemplateRequiredExternalRefRule("dwell template needs this field"),
            DwellTemplateRequiredCategoryRule("dwell template needs an entry"),
            DwellTemplateCodeLengthRule(255),
        ),
    )
}
