package com.acme.transport.crewing.swap.bundle

/** A rule a capped swap template must pass. Check returns its problems, or none. */
interface CappedSwapTemplateRule {
    fun check(item: CappedSwapTemplate): MutableList<String>
}

data class CappedSwapTemplateRequiredNameRule(var message: String) : CappedSwapTemplateRule {
    override fun check(item: CappedSwapTemplate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedSwapTemplateNameLengthRule(var maxLength: Int) : CappedSwapTemplateRule {
    override fun check(item: CappedSwapTemplate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedSwapTemplateRatioRangeRule(var min: Double, var max: Double) : CappedSwapTemplateRule {
    override fun check(item: CappedSwapTemplate): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped swap template. */
data class CappedSwapTemplateValidator(var rules: MutableList<CappedSwapTemplateRule>) {
    fun validate(item: CappedSwapTemplate): MutableList<String> {
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

    fun requireValid(item: CappedSwapTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedSwapTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedSwapTemplateValidator(): CappedSwapTemplateValidator {
    return CappedSwapTemplateValidator(
        mutableListOf<CappedSwapTemplateRule>(
            CappedSwapTemplateRequiredNameRule("capped swap template needs a value"),
            CappedSwapTemplateNameLengthRule(80),
            CappedSwapTemplateRatioRangeRule(0.3, 3426.5),
        ),
    )
}
