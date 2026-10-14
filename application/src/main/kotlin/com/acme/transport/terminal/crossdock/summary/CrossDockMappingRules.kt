package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.text.normalize

/** A rule a cross dock mapping must pass. Check returns its problems, or none. */
interface CrossDockMappingRule {
    fun check(item: CrossDockMapping): MutableList<String>
}

data class CrossDockMappingAllowedUnitRule(var allowed: MutableList<String>) : CrossDockMappingRule {
    override fun check(item: CrossDockMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is missing")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockMappingRequiredTitleRule(var message: String) : CrossDockMappingRule {
    override fun check(item: CrossDockMapping): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockMappingApprovedGuardRule(var limit: Int) : CrossDockMappingRule {
    override fun check(item: CrossDockMapping): MutableList<String> {
        if (item.approved && item.durationMinutes > limit) {
            return mutableListOf<String>("duration minutes ${item.durationMinutes} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock mapping. */
data class CrossDockMappingValidator(var rules: MutableList<CrossDockMappingRule>) {
    fun validate(item: CrossDockMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockMappingValidator(): CrossDockMappingValidator {
    return CrossDockMappingValidator(
        mutableListOf<CrossDockMappingRule>(
            CrossDockMappingAllowedUnitRule(mutableListOf<String>("capped", "seasonal", "default")),
            CrossDockMappingRequiredTitleRule("cross dock mapping needs a value"),
            CrossDockMappingApprovedGuardRule(120),
        ),
    )
}
