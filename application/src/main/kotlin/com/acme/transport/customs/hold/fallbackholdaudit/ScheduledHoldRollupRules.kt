package com.acme.transport.customs.hold.fallbackholdaudit

/** A rule a scheduled hold rollup must pass. Check returns its problems, or none. */
interface ScheduledHoldRollupRule {
    fun check(item: ScheduledHoldRollup): MutableList<String>
}

data class ScheduledHoldRollupTitleLengthRule(var maxLength: Int) : ScheduledHoldRollupRule {
    override fun check(item: ScheduledHoldRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldRollupSourceLengthRule(var maxLength: Int) : ScheduledHoldRollupRule {
    override fun check(item: ScheduledHoldRollup): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldRollupCodePrefixRule(var prefix: String) : ScheduledHoldRollupRule {
    override fun check(item: ScheduledHoldRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldRollupCategoryLengthRule(var maxLength: Int) : ScheduledHoldRollupRule {
    override fun check(item: ScheduledHoldRollup): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled hold rollup. */
data class ScheduledHoldRollupValidator(var rules: MutableList<ScheduledHoldRollupRule>) {
    fun validate(item: ScheduledHoldRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledHoldRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledHoldRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledHoldRollupValidator(): ScheduledHoldRollupValidator {
    return ScheduledHoldRollupValidator(
        mutableListOf<ScheduledHoldRollupRule>(
            ScheduledHoldRollupTitleLengthRule(3600),
            ScheduledHoldRollupSourceLengthRule(1000),
            ScheduledHoldRollupCodePrefixRule("OPS"),
            ScheduledHoldRollupCategoryLengthRule(255),
        ),
    )
}
