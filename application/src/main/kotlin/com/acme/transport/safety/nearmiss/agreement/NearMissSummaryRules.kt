package com.acme.transport.safety.nearmiss.agreement

/** A rule a near miss summary must pass. Check returns its problems, or none. */
interface NearMissSummaryRule {
    fun check(item: NearMissSummary): MutableList<String>
}

data class NearMissSummaryLimitRangeRule(var min: Int, var max: Int) : NearMissSummaryRule {
    override fun check(item: NearMissSummary): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class NearMissSummaryVisibleGuardRule(var limit: Double) : NearMissSummaryRule {
    override fun check(item: NearMissSummary): MutableList<String> {
        if (item.visible && item.cost > limit) {
            return mutableListOf<String>("cost ${item.cost} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class NearMissSummaryNameLengthRule(var maxLength: Int) : NearMissSummaryRule {
    override fun check(item: NearMissSummary): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissSummaryVersionRangeRule(var min: Int, var max: Int) : NearMissSummaryRule {
    override fun check(item: NearMissSummary): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a near miss summary. */
data class NearMissSummaryValidator(var rules: MutableList<NearMissSummaryRule>) {
    fun validate(item: NearMissSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NearMissSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissSummaryValidator(): NearMissSummaryValidator {
    return NearMissSummaryValidator(
        mutableListOf<NearMissSummaryRule>(
            NearMissSummaryLimitRangeRule(64, 255),
            NearMissSummaryVisibleGuardRule(5.4),
            NearMissSummaryNameLengthRule(32),
            NearMissSummaryVersionRangeRule(80, 3600),
        ),
    )
}
