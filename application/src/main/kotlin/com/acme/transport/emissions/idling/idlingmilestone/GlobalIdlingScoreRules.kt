package com.acme.transport.emissions.idling.idlingmilestone

/** A rule a global idling score must pass. Check returns its problems, or none. */
interface GlobalIdlingScoreRule {
    fun check(item: GlobalIdlingScore): MutableList<String>
}

data class GlobalIdlingScoreRequiredOwnerIdRule(var message: String) : GlobalIdlingScoreRule {
    override fun check(item: GlobalIdlingScore): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalIdlingScoreNameLengthRule(var maxLength: Int) : GlobalIdlingScoreRule {
    override fun check(item: GlobalIdlingScore): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalIdlingScoreRequiredRegionRule(var message: String) : GlobalIdlingScoreRule {
    override fun check(item: GlobalIdlingScore): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalIdlingScoreCodePrefixRule(var prefix: String) : GlobalIdlingScoreRule {
    override fun check(item: GlobalIdlingScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global idling score. */
data class GlobalIdlingScoreValidator(var rules: MutableList<GlobalIdlingScoreRule>) {
    fun validate(item: GlobalIdlingScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalIdlingScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalIdlingScore>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalIdlingScoreValidator(): GlobalIdlingScoreValidator {
    return GlobalIdlingScoreValidator(
        mutableListOf<GlobalIdlingScoreRule>(
            GlobalIdlingScoreRequiredOwnerIdRule("global idling score needs an entry"),
            GlobalIdlingScoreNameLengthRule(255),
            GlobalIdlingScoreRequiredRegionRule("global idling score needs a value"),
            GlobalIdlingScoreCodePrefixRule("ACM"),
        ),
    )
}
