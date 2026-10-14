package com.acme.transport.network.hub.hubdraft

import com.acme.transport.kit.text.normalize

/** A rule a manual hub rollup must pass. Check returns its problems, or none. */
interface ManualHubRollupRule {
    fun check(item: ManualHubRollup): MutableList<String>
}

data class ManualHubRollupRetriesRangeRule(var min: Int, var max: Int) : ManualHubRollupRule {
    override fun check(item: ManualHubRollup): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class ManualHubRollupRequiredCodeRule(var message: String) : ManualHubRollupRule {
    override fun check(item: ManualHubRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualHubRollupCodePrefixRule(var prefix: String) : ManualHubRollupRule {
    override fun check(item: ManualHubRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualHubRollupRequiredShiftCodeRule(var message: String) : ManualHubRollupRule {
    override fun check(item: ManualHubRollup): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualHubRollupAllowedCodeRule(var allowed: MutableList<String>) : ManualHubRollupRule {
    override fun check(item: ManualHubRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual hub rollup. */
data class ManualHubRollupValidator(var rules: MutableList<ManualHubRollupRule>) {
    fun validate(item: ManualHubRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualHubRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualHubRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultManualHubRollupValidator(): ManualHubRollupValidator {
    return ManualHubRollupValidator(
        mutableListOf<ManualHubRollupRule>(
            ManualHubRollupRetriesRangeRule(16, 80),
            ManualHubRollupRequiredCodeRule("manual hub rollup needs an entry"),
            ManualHubRollupCodePrefixRule("Q"),
            ManualHubRollupRequiredShiftCodeRule("manual hub rollup needs an entry"),
            ManualHubRollupAllowedCodeRule(mutableListOf<String>("primary", "primary", "capped")),
        ),
    )
}
