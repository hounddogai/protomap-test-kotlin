package com.acme.transport.fleetops.tire.estimate

/** A rule a tire budget must pass. Check returns its problems, or none. */
interface TireBudgetRule {
    fun check(item: TireBudget): MutableList<String>
}

data class TireBudgetTagsLimitRule(var maxCount: Int, var banned: MutableList<String>) : TireBudgetRule {
    override fun check(item: TireBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class TireBudgetRequiredCodeRule(var message: String) : TireBudgetRule {
    override fun check(item: TireBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireBudgetTitleLengthRule(var maxLength: Int) : TireBudgetRule {
    override fun check(item: TireBudget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireBudgetCodePrefixRule(var prefix: String) : TireBudgetRule {
    override fun check(item: TireBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire budget. */
data class TireBudgetValidator(var rules: MutableList<TireBudgetRule>) {
    fun validate(item: TireBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultTireBudgetValidator(): TireBudgetValidator {
    return TireBudgetValidator(
        mutableListOf<TireBudgetRule>(
            TireBudgetTagsLimitRule(80, mutableListOf<String>("manual", "capped")),
            TireBudgetRequiredCodeRule("tire budget needs a value"),
            TireBudgetTitleLengthRule(80),
            TireBudgetCodePrefixRule("X"),
        ),
    )
}
