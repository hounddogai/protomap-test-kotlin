package com.acme.transport.network.transittime.allocation

/** A rule a default transit time group must pass. Check returns its problems, or none. */
interface DefaultTransitTimeGroupRule {
    fun check(item: DefaultTransitTimeGroup): MutableList<String>
}

data class DefaultTransitTimeGroupRequiredDockRule(var message: String) : DefaultTransitTimeGroupRule {
    override fun check(item: DefaultTransitTimeGroup): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeGroupLabelLengthRule(var maxLength: Int) : DefaultTransitTimeGroupRule {
    override fun check(item: DefaultTransitTimeGroup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeGroupPriorityRangeRule(var min: Int, var max: Int) : DefaultTransitTimeGroupRule {
    override fun check(item: DefaultTransitTimeGroup): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class DefaultTransitTimeGroupRequiredCodeRule(var message: String) : DefaultTransitTimeGroupRule {
    override fun check(item: DefaultTransitTimeGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeGroupMarginRangeRule(
    var min: Double,
    var max: Double,
) : DefaultTransitTimeGroupRule {
    override fun check(item: DefaultTransitTimeGroup): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeGroupContactEmailLengthRule(var maxLength: Int) : DefaultTransitTimeGroupRule {
    override fun check(item: DefaultTransitTimeGroup): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default transit time group. */
data class DefaultTransitTimeGroupValidator(var rules: MutableList<DefaultTransitTimeGroupRule>) {
    fun validate(item: DefaultTransitTimeGroup): MutableList<String> {
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

    fun requireValid(item: DefaultTransitTimeGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTransitTimeGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTransitTimeGroupValidator(): DefaultTransitTimeGroupValidator {
    return DefaultTransitTimeGroupValidator(
        mutableListOf<DefaultTransitTimeGroupRule>(
            DefaultTransitTimeGroupRequiredDockRule("default transit time group needs a value"),
            DefaultTransitTimeGroupLabelLengthRule(3600),
            DefaultTransitTimeGroupPriorityRangeRule(80, 255),
            DefaultTransitTimeGroupRequiredCodeRule("default transit time group needs a value"),
            DefaultTransitTimeGroupMarginRangeRule(6.6, 2654.8),
            DefaultTransitTimeGroupContactEmailLengthRule(120),
        ),
    )
}
