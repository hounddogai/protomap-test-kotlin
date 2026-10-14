package com.acme.transport.crewing.roster.quota

/** A rule a roster quota must pass. Check returns its problems, or none. */
interface RosterQuotaRule {
    fun check(item: RosterQuota): MutableList<String>
}

data class RosterQuotaRequiredTitleRule(var message: String) : RosterQuotaRule {
    override fun check(item: RosterQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterQuotaCodePrefixRule(var prefix: String) : RosterQuotaRule {
    override fun check(item: RosterQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RosterQuotaCodeLengthRule(var maxLength: Int) : RosterQuotaRule {
    override fun check(item: RosterQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterQuotaRequiredTimezoneRule(var message: String) : RosterQuotaRule {
    override fun check(item: RosterQuota): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterQuotaRequiredCodeRule(var message: String) : RosterQuotaRule {
    override fun check(item: RosterQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster quota. */
data class RosterQuotaValidator(var rules: MutableList<RosterQuotaRule>) {
    fun validate(item: RosterQuota): MutableList<String> {
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

    fun requireValid(item: RosterQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterQuotaValidator(): RosterQuotaValidator {
    return RosterQuotaValidator(
        mutableListOf<RosterQuotaRule>(
            RosterQuotaRequiredTitleRule("roster quota needs a value"),
            RosterQuotaCodePrefixRule("ACM"),
            RosterQuotaCodeLengthRule(16),
            RosterQuotaRequiredTimezoneRule("roster quota needs this field"),
            RosterQuotaRequiredCodeRule("roster quota needs a value"),
        ),
    )
}
