package com.acme.transport.crewing.restbreak.profile

/** A rule a rest break profile must pass. Check returns its problems, or none. */
interface RestBreakProfileRule {
    fun check(item: RestBreakProfile): MutableList<String>
}

data class RestBreakProfileStopCountRangeRule(var min: Int, var max: Int) : RestBreakProfileRule {
    override fun check(item: RestBreakProfile): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class RestBreakProfileQuantityRangeRule(var min: Int, var max: Int) : RestBreakProfileRule {
    override fun check(item: RestBreakProfile): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakProfileCodeLengthRule(var maxLength: Int) : RestBreakProfileRule {
    override fun check(item: RestBreakProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakProfileRequiredNameRule(var message: String) : RestBreakProfileRule {
    override fun check(item: RestBreakProfile): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakProfileRouteCodeLengthRule(var maxLength: Int) : RestBreakProfileRule {
    override fun check(item: RestBreakProfile): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakProfileRequiredCodeRule(var message: String) : RestBreakProfileRule {
    override fun check(item: RestBreakProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break profile. */
data class RestBreakProfileValidator(var rules: MutableList<RestBreakProfileRule>) {
    fun validate(item: RestBreakProfile): MutableList<String> {
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

    fun requireValid(item: RestBreakProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakProfileValidator(): RestBreakProfileValidator {
    return RestBreakProfileValidator(
        mutableListOf<RestBreakProfileRule>(
            RestBreakProfileStopCountRangeRule(255, 1000),
            RestBreakProfileQuantityRangeRule(16, 32),
            RestBreakProfileCodeLengthRule(64),
            RestBreakProfileRequiredNameRule("rest break profile needs this field"),
            RestBreakProfileRouteCodeLengthRule(1000),
            RestBreakProfileRequiredCodeRule("rest break profile needs a value"),
        ),
    )
}
