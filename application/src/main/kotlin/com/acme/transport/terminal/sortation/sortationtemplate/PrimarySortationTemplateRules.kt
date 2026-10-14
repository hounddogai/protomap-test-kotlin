package com.acme.transport.terminal.sortation.sortationtemplate

/** A rule a primary sortation template must pass. Check returns its problems, or none. */
interface PrimarySortationTemplateRule {
    fun check(item: PrimarySortationTemplate): MutableList<String>
}

data class PrimarySortationTemplateRequiredLaneRule(var message: String) : PrimarySortationTemplateRule {
    override fun check(item: PrimarySortationTemplate): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimarySortationTemplateEndsAtRangeRule(var min: Int, var max: Int) : PrimarySortationTemplateRule {
    override fun check(item: PrimarySortationTemplate): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class PrimarySortationTemplateLaneLengthRule(var maxLength: Int) : PrimarySortationTemplateRule {
    override fun check(item: PrimarySortationTemplate): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimarySortationTemplateMarginRangeRule(
    var min: Double,
    var max: Double,
) : PrimarySortationTemplateRule {
    override fun check(item: PrimarySortationTemplate): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class PrimarySortationTemplateCodePrefixRule(var prefix: String) : PrimarySortationTemplateRule {
    override fun check(item: PrimarySortationTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary sortation template. */
data class PrimarySortationTemplateValidator(var rules: MutableList<PrimarySortationTemplateRule>) {
    fun validate(item: PrimarySortationTemplate): MutableList<String> {
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

    fun requireValid(item: PrimarySortationTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimarySortationTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimarySortationTemplateValidator(): PrimarySortationTemplateValidator {
    return PrimarySortationTemplateValidator(
        mutableListOf<PrimarySortationTemplateRule>(
            PrimarySortationTemplateRequiredLaneRule("primary sortation template needs an entry"),
            PrimarySortationTemplateEndsAtRangeRule(32, 1000),
            PrimarySortationTemplateLaneLengthRule(16),
            PrimarySortationTemplateMarginRangeRule(9.5, 3684.1),
            PrimarySortationTemplateCodePrefixRule("REF"),
        ),
    )
}
