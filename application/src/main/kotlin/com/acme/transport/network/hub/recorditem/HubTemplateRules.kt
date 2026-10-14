package com.acme.transport.network.hub.recorditem

/** A rule a hub template must pass. Check returns its problems, or none. */
interface HubTemplateRule {
    fun check(item: HubTemplate): MutableList<String>
}

data class HubTemplateRatioRangeRule(var min: Double, var max: Double) : HubTemplateRule {
    override fun check(item: HubTemplate): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HubTemplateCodeLengthRule(var maxLength: Int) : HubTemplateRule {
    override fun check(item: HubTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubTemplateMarginRangeRule(var min: Double, var max: Double) : HubTemplateRule {
    override fun check(item: HubTemplate): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub template. */
data class HubTemplateValidator(var rules: MutableList<HubTemplateRule>) {
    fun validate(item: HubTemplate): MutableList<String> {
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

    fun requireValid(item: HubTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultHubTemplateValidator(): HubTemplateValidator {
    return HubTemplateValidator(
        mutableListOf<HubTemplateRule>(
            HubTemplateRatioRangeRule(1.3, 4759.2),
            HubTemplateCodeLengthRule(120),
            HubTemplateMarginRangeRule(7.5, 4395.2),
        ),
    )
}
