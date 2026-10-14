package com.acme.transport.linehaul.tender.summary

/** A rule a global tender digest must pass. Check returns its problems, or none. */
interface GlobalTenderDigestRule {
    fun check(item: GlobalTenderDigest): MutableList<String>
}

data class GlobalTenderDigestCodePrefixRule(var prefix: String) : GlobalTenderDigestRule {
    override fun check(item: GlobalTenderDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderDigestRequiredNotesRule(var message: String) : GlobalTenderDigestRule {
    override fun check(item: GlobalTenderDigest): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderDigestActiveGuardRule(var limit: Int) : GlobalTenderDigestRule {
    override fun check(item: GlobalTenderDigest): MutableList<String> {
        if (item.active && item.revision > limit) {
            return mutableListOf<String>("revision ${item.revision} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderDigestTitleLengthRule(var maxLength: Int) : GlobalTenderDigestRule {
    override fun check(item: GlobalTenderDigest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderDigestCategoryLengthRule(var maxLength: Int) : GlobalTenderDigestRule {
    override fun check(item: GlobalTenderDigest): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global tender digest. */
data class GlobalTenderDigestValidator(var rules: MutableList<GlobalTenderDigestRule>) {
    fun validate(item: GlobalTenderDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalTenderDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTenderDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTenderDigestValidator(): GlobalTenderDigestValidator {
    return GlobalTenderDigestValidator(
        mutableListOf<GlobalTenderDigestRule>(
            GlobalTenderDigestCodePrefixRule("ACM"),
            GlobalTenderDigestRequiredNotesRule("global tender digest needs this field"),
            GlobalTenderDigestActiveGuardRule(80),
            GlobalTenderDigestTitleLengthRule(86400),
            GlobalTenderDigestCategoryLengthRule(3600),
        ),
    )
}
