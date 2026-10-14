package com.acme.transport.crewing.roster.score

/** A rule a global roster group must pass. Check returns its problems, or none. */
interface GlobalRosterGroupRule {
    fun check(item: GlobalRosterGroup): MutableList<String>
}

data class GlobalRosterGroupDockLengthRule(var maxLength: Int) : GlobalRosterGroupRule {
    override fun check(item: GlobalRosterGroup): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRosterGroupRequiredDockRule(var message: String) : GlobalRosterGroupRule {
    override fun check(item: GlobalRosterGroup): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRosterGroupRequiredSummaryRule(var message: String) : GlobalRosterGroupRule {
    override fun check(item: GlobalRosterGroup): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRosterGroupTitleLengthRule(var maxLength: Int) : GlobalRosterGroupRule {
    override fun check(item: GlobalRosterGroup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRosterGroupCodePrefixRule(var prefix: String) : GlobalRosterGroupRule {
    override fun check(item: GlobalRosterGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRosterGroupSummaryLengthRule(var maxLength: Int) : GlobalRosterGroupRule {
    override fun check(item: GlobalRosterGroup): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global roster group. */
data class GlobalRosterGroupValidator(var rules: MutableList<GlobalRosterGroupRule>) {
    fun validate(item: GlobalRosterGroup): MutableList<String> {
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

    fun requireValid(item: GlobalRosterGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRosterGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRosterGroupValidator(): GlobalRosterGroupValidator {
    return GlobalRosterGroupValidator(
        mutableListOf<GlobalRosterGroupRule>(
            GlobalRosterGroupDockLengthRule(3600),
            GlobalRosterGroupRequiredDockRule("global roster group needs this field"),
            GlobalRosterGroupRequiredSummaryRule("global roster group needs this field"),
            GlobalRosterGroupTitleLengthRule(80),
            GlobalRosterGroupCodePrefixRule("X"),
            GlobalRosterGroupSummaryLengthRule(255),
        ),
    )
}
