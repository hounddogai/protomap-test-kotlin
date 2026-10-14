package com.acme.transport.crewing.restbreak.schedule

import com.acme.transport.kit.text.normalize

/** A rule a global rest break profile must pass. Check returns its problems, or none. */
interface GlobalRestBreakProfileRule {
    fun check(item: GlobalRestBreakProfile): MutableList<String>
}

data class GlobalRestBreakProfileRequiredNameRule(var message: String) : GlobalRestBreakProfileRule {
    override fun check(item: GlobalRestBreakProfile): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRestBreakProfileAllowedNameRule(
    var allowed: MutableList<String>,
) : GlobalRestBreakProfileRule {
    override fun check(item: GlobalRestBreakProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRestBreakProfileCodePrefixRule(var prefix: String) : GlobalRestBreakProfileRule {
    override fun check(item: GlobalRestBreakProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global rest break profile. */
data class GlobalRestBreakProfileValidator(var rules: MutableList<GlobalRestBreakProfileRule>) {
    fun validate(item: GlobalRestBreakProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalRestBreakProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRestBreakProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRestBreakProfileValidator(): GlobalRestBreakProfileValidator {
    return GlobalRestBreakProfileValidator(
        mutableListOf<GlobalRestBreakProfileRule>(
            GlobalRestBreakProfileRequiredNameRule("global rest break profile needs this field"),
            GlobalRestBreakProfileAllowedNameRule(mutableListOf<String>()),
            GlobalRestBreakProfileCodePrefixRule("REF"),
        ),
    )
}
