package com.acme.transport.safety.cameraevent.rollup

/** A rule a primary camera event target must pass. Check returns its problems, or none. */
interface PrimaryCameraEventTargetRule {
    fun check(item: PrimaryCameraEventTarget): MutableList<String>
}

data class PrimaryCameraEventTargetRequiredCategoryRule(var message: String) : PrimaryCameraEventTargetRule {
    override fun check(item: PrimaryCameraEventTarget): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCameraEventTargetRequiredTitleRule(var message: String) : PrimaryCameraEventTargetRule {
    override fun check(item: PrimaryCameraEventTarget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCameraEventTargetMarginRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryCameraEventTargetRule {
    override fun check(item: PrimaryCameraEventTarget): MutableList<String> {
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

/** Runs every rule of a primary camera event target. */
data class PrimaryCameraEventTargetValidator(var rules: MutableList<PrimaryCameraEventTargetRule>) {
    fun validate(item: PrimaryCameraEventTarget): MutableList<String> {
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

    fun requireValid(item: PrimaryCameraEventTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCameraEventTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCameraEventTargetValidator(): PrimaryCameraEventTargetValidator {
    return PrimaryCameraEventTargetValidator(
        mutableListOf<PrimaryCameraEventTargetRule>(
            PrimaryCameraEventTargetRequiredCategoryRule("primary camera event target needs a value"),
            PrimaryCameraEventTargetRequiredTitleRule("primary camera event target needs a value"),
            PrimaryCameraEventTargetMarginRangeRule(9.9, 3151.1),
        ),
    )
}
