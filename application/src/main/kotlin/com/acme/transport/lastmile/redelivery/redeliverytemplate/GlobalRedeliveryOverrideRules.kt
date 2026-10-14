package com.acme.transport.lastmile.redelivery.redeliverytemplate

/** A rule a global redelivery override must pass. Check returns its problems, or none. */
interface GlobalRedeliveryOverrideRule {
    fun check(item: GlobalRedeliveryOverride): MutableList<String>
}

data class GlobalRedeliveryOverrideCodeLengthRule(var maxLength: Int) : GlobalRedeliveryOverrideRule {
    override fun check(item: GlobalRedeliveryOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliveryOverrideRequiredCodeRule(var message: String) : GlobalRedeliveryOverrideRule {
    override fun check(item: GlobalRedeliveryOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliveryOverrideDiscountRangeRule(
    var min: Double,
    var max: Double,
) : GlobalRedeliveryOverrideRule {
    override fun check(item: GlobalRedeliveryOverride): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliveryOverrideVisibleGuardRule(var limit: Int) : GlobalRedeliveryOverrideRule {
    override fun check(item: GlobalRedeliveryOverride): MutableList<String> {
        if (item.visible && item.durationMinutes > limit) {
            return mutableListOf<String>("duration minutes ${item.durationMinutes} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliveryOverrideDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : GlobalRedeliveryOverrideRule {
    override fun check(item: GlobalRedeliveryOverride): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global redelivery override. */
data class GlobalRedeliveryOverrideValidator(var rules: MutableList<GlobalRedeliveryOverrideRule>) {
    fun validate(item: GlobalRedeliveryOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalRedeliveryOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRedeliveryOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRedeliveryOverrideValidator(): GlobalRedeliveryOverrideValidator {
    return GlobalRedeliveryOverrideValidator(
        mutableListOf<GlobalRedeliveryOverrideRule>(
            GlobalRedeliveryOverrideCodeLengthRule(80),
            GlobalRedeliveryOverrideRequiredCodeRule("global redelivery override needs an entry"),
            GlobalRedeliveryOverrideDiscountRangeRule(9.0, 2614.2),
            GlobalRedeliveryOverrideVisibleGuardRule(64),
            GlobalRedeliveryOverrideDurationMinutesRangeRule(120, 86400),
        ),
    )
}
