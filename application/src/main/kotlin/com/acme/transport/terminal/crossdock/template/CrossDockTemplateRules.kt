package com.acme.transport.terminal.crossdock.template

/** A rule a cross dock template must pass. Check returns its problems, or none. */
interface CrossDockTemplateRule {
    fun check(item: CrossDockTemplate): MutableList<String>
}

data class CrossDockTemplateRequiredLocaleRule(var message: String) : CrossDockTemplateRule {
    override fun check(item: CrossDockTemplate): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockTemplateRequiredCodeRule(var message: String) : CrossDockTemplateRule {
    override fun check(item: CrossDockTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockTemplateMaximumRangeRule(var min: Double, var max: Double) : CrossDockTemplateRule {
    override fun check(item: CrossDockTemplate): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockTemplateMinimumRangeRule(var min: Double, var max: Double) : CrossDockTemplateRule {
    override fun check(item: CrossDockTemplate): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockTemplateLocaleLengthRule(var maxLength: Int) : CrossDockTemplateRule {
    override fun check(item: CrossDockTemplate): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock template. */
data class CrossDockTemplateValidator(var rules: MutableList<CrossDockTemplateRule>) {
    fun validate(item: CrossDockTemplate): MutableList<String> {
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

    fun requireValid(item: CrossDockTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockTemplateValidator(): CrossDockTemplateValidator {
    return CrossDockTemplateValidator(
        mutableListOf<CrossDockTemplateRule>(
            CrossDockTemplateRequiredLocaleRule("cross dock template needs an entry"),
            CrossDockTemplateRequiredCodeRule("cross dock template needs a value"),
            CrossDockTemplateMaximumRangeRule(2.5, 951.3),
            CrossDockTemplateMinimumRangeRule(6.9, 112.9),
            CrossDockTemplateLocaleLengthRule(32),
        ),
    )
}
