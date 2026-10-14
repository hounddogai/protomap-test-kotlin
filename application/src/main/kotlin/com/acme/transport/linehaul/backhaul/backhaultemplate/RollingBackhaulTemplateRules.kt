package com.acme.transport.linehaul.backhaul.backhaultemplate

/** A rule a rolling backhaul template must pass. Check returns its problems, or none. */
interface RollingBackhaulTemplateRule {
    fun check(item: RollingBackhaulTemplate): MutableList<String>
}

data class RollingBackhaulTemplateCapacityRangeRule(var min: Int, var max: Int) : RollingBackhaulTemplateRule {
    override fun check(item: RollingBackhaulTemplate): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class RollingBackhaulTemplateTimezoneLengthRule(var maxLength: Int) : RollingBackhaulTemplateRule {
    override fun check(item: RollingBackhaulTemplate): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingBackhaulTemplateRequiredCodeRule(var message: String) : RollingBackhaulTemplateRule {
    override fun check(item: RollingBackhaulTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling backhaul template. */
data class RollingBackhaulTemplateValidator(var rules: MutableList<RollingBackhaulTemplateRule>) {
    fun validate(item: RollingBackhaulTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingBackhaulTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingBackhaulTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingBackhaulTemplateValidator(): RollingBackhaulTemplateValidator {
    return RollingBackhaulTemplateValidator(
        mutableListOf<RollingBackhaulTemplateRule>(
            RollingBackhaulTemplateCapacityRangeRule(1000, 3600),
            RollingBackhaulTemplateTimezoneLengthRule(16),
            RollingBackhaulTemplateRequiredCodeRule("rolling backhaul template needs an entry"),
        ),
    )
}
