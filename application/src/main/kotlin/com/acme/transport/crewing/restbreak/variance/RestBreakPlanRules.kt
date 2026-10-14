package com.acme.transport.crewing.restbreak.variance

/** A rule a rest break plan must pass. Check returns its problems, or none. */
interface RestBreakPlanRule {
    fun check(item: RestBreakPlan): MutableList<String>
}

data class RestBreakPlanRequiredNameRule(var message: String) : RestBreakPlanRule {
    override fun check(item: RestBreakPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakPlanCodePrefixRule(var prefix: String) : RestBreakPlanRule {
    override fun check(item: RestBreakPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakPlanRequiredTimezoneRule(var message: String) : RestBreakPlanRule {
    override fun check(item: RestBreakPlan): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakPlanSourceLengthRule(var maxLength: Int) : RestBreakPlanRule {
    override fun check(item: RestBreakPlan): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break plan. */
data class RestBreakPlanValidator(var rules: MutableList<RestBreakPlanRule>) {
    fun validate(item: RestBreakPlan): MutableList<String> {
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

    fun requireValid(item: RestBreakPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakPlanValidator(): RestBreakPlanValidator {
    return RestBreakPlanValidator(
        mutableListOf<RestBreakPlanRule>(
            RestBreakPlanRequiredNameRule("rest break plan needs an entry"),
            RestBreakPlanCodePrefixRule("OPS"),
            RestBreakPlanRequiredTimezoneRule("rest break plan needs this field"),
            RestBreakPlanSourceLengthRule(255),
        ),
    )
}
