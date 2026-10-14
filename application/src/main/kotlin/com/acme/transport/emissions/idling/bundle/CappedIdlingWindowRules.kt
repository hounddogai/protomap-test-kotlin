package com.acme.transport.emissions.idling.bundle

import com.acme.transport.kit.text.normalize

/** A rule a capped idling window must pass. Check returns its problems, or none. */
interface CappedIdlingWindowRule {
    fun check(item: CappedIdlingWindow): MutableList<String>
}

data class CappedIdlingWindowCodePrefixRule(var prefix: String) : CappedIdlingWindowRule {
    override fun check(item: CappedIdlingWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedIdlingWindowAllowedCodeRule(var allowed: MutableList<String>) : CappedIdlingWindowRule {
    override fun check(item: CappedIdlingWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class CappedIdlingWindowRequiredLaneRule(var message: String) : CappedIdlingWindowRule {
    override fun check(item: CappedIdlingWindow): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped idling window. */
data class CappedIdlingWindowValidator(var rules: MutableList<CappedIdlingWindowRule>) {
    fun validate(item: CappedIdlingWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedIdlingWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedIdlingWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedIdlingWindowValidator(): CappedIdlingWindowValidator {
    return CappedIdlingWindowValidator(
        mutableListOf<CappedIdlingWindowRule>(
            CappedIdlingWindowCodePrefixRule("ACM"),
            CappedIdlingWindowAllowedCodeRule(mutableListOf<String>("adaptive", "default", "tiered")),
            CappedIdlingWindowRequiredLaneRule("capped idling window needs this field"),
        ),
    )
}
