package com.acme.transport.safety.violation.cycle

/** A rule a violation template must pass. Check returns its problems, or none. */
interface ViolationTemplateRule {
    fun check(item: ViolationTemplate): MutableList<String>
}

data class ViolationTemplateCodePrefixRule(var prefix: String) : ViolationTemplateRule {
    override fun check(item: ViolationTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ViolationTemplateGraceDaysRangeRule(var min: Int, var max: Int) : ViolationTemplateRule {
    override fun check(item: ViolationTemplate): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class ViolationTemplateMaximumRangeRule(var min: Double, var max: Double) : ViolationTemplateRule {
    override fun check(item: ViolationTemplate): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation template. */
data class ViolationTemplateValidator(var rules: MutableList<ViolationTemplateRule>) {
    fun validate(item: ViolationTemplate): MutableList<String> {
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

    fun requireValid(item: ViolationTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationTemplateValidator(): ViolationTemplateValidator {
    return ViolationTemplateValidator(
        mutableListOf<ViolationTemplateRule>(
            ViolationTemplateCodePrefixRule("OPS"),
            ViolationTemplateGraceDaysRangeRule(32, 1000),
            ViolationTemplateMaximumRangeRule(1.1, 179.2),
        ),
    )
}
