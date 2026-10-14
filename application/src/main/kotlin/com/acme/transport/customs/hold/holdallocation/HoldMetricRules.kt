package com.acme.transport.customs.hold.holdallocation

/** A rule a hold metric must pass. Check returns its problems, or none. */
interface HoldMetricRule {
    fun check(item: HoldMetric): MutableList<String>
}

data class HoldMetricRequiredCodeRule(var message: String) : HoldMetricRule {
    override fun check(item: HoldMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldMetricRequiredNotesRule(var message: String) : HoldMetricRule {
    override fun check(item: HoldMetric): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldMetricCodePrefixRule(var prefix: String) : HoldMetricRule {
    override fun check(item: HoldMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HoldMetricVisibleGuardRule(var limit: Double) : HoldMetricRule {
    override fun check(item: HoldMetric): MutableList<String> {
        if (item.visible && item.cost > limit) {
            return mutableListOf<String>("cost ${item.cost} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class HoldMetricNotesLengthRule(var maxLength: Int) : HoldMetricRule {
    override fun check(item: HoldMetric): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold metric. */
data class HoldMetricValidator(var rules: MutableList<HoldMetricRule>) {
    fun validate(item: HoldMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HoldMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldMetricValidator(): HoldMetricValidator {
    return HoldMetricValidator(
        mutableListOf<HoldMetricRule>(
            HoldMetricRequiredCodeRule("hold metric needs an entry"),
            HoldMetricRequiredNotesRule("hold metric needs an entry"),
            HoldMetricCodePrefixRule("X"),
            HoldMetricVisibleGuardRule(0.3),
            HoldMetricNotesLengthRule(80),
        ),
    )
}
