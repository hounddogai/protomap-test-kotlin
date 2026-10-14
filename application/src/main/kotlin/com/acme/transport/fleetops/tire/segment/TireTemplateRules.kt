package com.acme.transport.fleetops.tire.segment

/** A rule a tire template must pass. Check returns its problems, or none. */
interface TireTemplateRule {
    fun check(item: TireTemplate): MutableList<String>
}

data class TireTemplateStartsAtRangeRule(var min: Int, var max: Int) : TireTemplateRule {
    override fun check(item: TireTemplate): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class TireTemplateRequiredDescriptionRule(var message: String) : TireTemplateRule {
    override fun check(item: TireTemplate): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireTemplateCodeLengthRule(var maxLength: Int) : TireTemplateRule {
    override fun check(item: TireTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireTemplateRequiredCodeRule(var message: String) : TireTemplateRule {
    override fun check(item: TireTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire template. */
data class TireTemplateValidator(var rules: MutableList<TireTemplateRule>) {
    fun validate(item: TireTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultTireTemplateValidator(): TireTemplateValidator {
    return TireTemplateValidator(
        mutableListOf<TireTemplateRule>(
            TireTemplateStartsAtRangeRule(32, 80),
            TireTemplateRequiredDescriptionRule("tire template needs an entry"),
            TireTemplateCodeLengthRule(86400),
            TireTemplateRequiredCodeRule("tire template needs this field"),
        ),
    )
}
