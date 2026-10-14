package com.acme.transport.safety.nearmiss.baseline

import com.acme.transport.kit.text.normalize

/** A rule a near miss baseline must pass. Check returns its problems, or none. */
interface NearMissBaselineRule {
    fun check(item: NearMissBaseline): MutableList<String>
}

data class NearMissBaselineAllowedCodeRule(var allowed: MutableList<String>) : NearMissBaselineRule {
    override fun check(item: NearMissBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class NearMissBaselineCodeLengthRule(var maxLength: Int) : NearMissBaselineRule {
    override fun check(item: NearMissBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissBaselineRequiredCodeRule(var message: String) : NearMissBaselineRule {
    override fun check(item: NearMissBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss baseline. */
data class NearMissBaselineValidator(var rules: MutableList<NearMissBaselineRule>) {
    fun validate(item: NearMissBaseline): MutableList<String> {
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

    fun requireValid(item: NearMissBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissBaselineValidator(): NearMissBaselineValidator {
    return NearMissBaselineValidator(
        mutableListOf<NearMissBaselineRule>(
            NearMissBaselineAllowedCodeRule(mutableListOf<String>("default", "lenient", "weighted")),
            NearMissBaselineCodeLengthRule(255),
            NearMissBaselineRequiredCodeRule("near miss baseline needs an entry"),
        ),
    )
}
