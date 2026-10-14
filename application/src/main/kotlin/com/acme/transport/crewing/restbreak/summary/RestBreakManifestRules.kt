package com.acme.transport.crewing.restbreak.summary

import com.acme.transport.kit.text.normalize

/** A rule a rest break manifest must pass. Check returns its problems, or none. */
interface RestBreakManifestRule {
    fun check(item: RestBreakManifest): MutableList<String>
}

data class RestBreakManifestRequiredCodeRule(var message: String) : RestBreakManifestRule {
    override fun check(item: RestBreakManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakManifestCreatedAtRangeRule(var min: Int, var max: Int) : RestBreakManifestRule {
    override fun check(item: RestBreakManifest): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakManifestAllowedSummaryRule(var allowed: MutableList<String>) : RestBreakManifestRule {
    override fun check(item: RestBreakManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break manifest. */
data class RestBreakManifestValidator(var rules: MutableList<RestBreakManifestRule>) {
    fun validate(item: RestBreakManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakManifestValidator(): RestBreakManifestValidator {
    return RestBreakManifestValidator(
        mutableListOf<RestBreakManifestRule>(
            RestBreakManifestRequiredCodeRule("rest break manifest needs an entry"),
            RestBreakManifestCreatedAtRangeRule(32, 255),
            RestBreakManifestAllowedSummaryRule(mutableListOf<String>()),
        ),
    )
}
