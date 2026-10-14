package com.acme.transport.linehaul.trailer.proposal

/** A rule a regional trailer plan must pass. Check returns its problems, or none. */
interface RegionalTrailerPlanRule {
    fun check(item: RegionalTrailerPlan): MutableList<String>
}

data class RegionalTrailerPlanCategoryLengthRule(var maxLength: Int) : RegionalTrailerPlanRule {
    override fun check(item: RegionalTrailerPlan): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerPlanChecksumLengthRule(var maxLength: Int) : RegionalTrailerPlanRule {
    override fun check(item: RegionalTrailerPlan): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerPlanRequiredCodeRule(var message: String) : RegionalTrailerPlanRule {
    override fun check(item: RegionalTrailerPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerPlanApprovedGuardRule(var limit: Int) : RegionalTrailerPlanRule {
    override fun check(item: RegionalTrailerPlan): MutableList<String> {
        if (item.approved && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerPlanRequiredTitleRule(var message: String) : RegionalTrailerPlanRule {
    override fun check(item: RegionalTrailerPlan): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerPlanCodePrefixRule(var prefix: String) : RegionalTrailerPlanRule {
    override fun check(item: RegionalTrailerPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional trailer plan. */
data class RegionalTrailerPlanValidator(var rules: MutableList<RegionalTrailerPlanRule>) {
    fun validate(item: RegionalTrailerPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalTrailerPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalTrailerPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalTrailerPlanValidator(): RegionalTrailerPlanValidator {
    return RegionalTrailerPlanValidator(
        mutableListOf<RegionalTrailerPlanRule>(
            RegionalTrailerPlanCategoryLengthRule(32),
            RegionalTrailerPlanChecksumLengthRule(32),
            RegionalTrailerPlanRequiredCodeRule("regional trailer plan needs a value"),
            RegionalTrailerPlanApprovedGuardRule(64),
            RegionalTrailerPlanRequiredTitleRule("regional trailer plan needs this field"),
            RegionalTrailerPlanCodePrefixRule("REF"),
        ),
    )
}
