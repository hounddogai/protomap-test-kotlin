package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

/** A rule a regional trailer milestone must pass. Check returns its problems, or none. */
interface RegionalTrailerMilestoneRule {
    fun check(item: RegionalTrailerMilestone): MutableList<String>
}

data class RegionalTrailerMilestoneCodePrefixRule(var prefix: String) : RegionalTrailerMilestoneRule {
    override fun check(item: RegionalTrailerMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerMilestoneRequiredTitleRule(var message: String) : RegionalTrailerMilestoneRule {
    override fun check(item: RegionalTrailerMilestone): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerMilestoneCodeLengthRule(var maxLength: Int) : RegionalTrailerMilestoneRule {
    override fun check(item: RegionalTrailerMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerMilestoneTitleLengthRule(var maxLength: Int) : RegionalTrailerMilestoneRule {
    override fun check(item: RegionalTrailerMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional trailer milestone. */
data class RegionalTrailerMilestoneValidator(var rules: MutableList<RegionalTrailerMilestoneRule>) {
    fun validate(item: RegionalTrailerMilestone): MutableList<String> {
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

    fun requireValid(item: RegionalTrailerMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalTrailerMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalTrailerMilestoneValidator(): RegionalTrailerMilestoneValidator {
    return RegionalTrailerMilestoneValidator(
        mutableListOf<RegionalTrailerMilestoneRule>(
            RegionalTrailerMilestoneCodePrefixRule("ACM"),
            RegionalTrailerMilestoneRequiredTitleRule("regional trailer milestone needs an entry"),
            RegionalTrailerMilestoneCodeLengthRule(3600),
            RegionalTrailerMilestoneTitleLengthRule(86400),
        ),
    )
}
