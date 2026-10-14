package com.acme.transport.customs.hold.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a lenient hold group must pass. Check returns its problems, or none. */
interface LenientHoldGroupRule {
    fun check(item: LenientHoldGroup): MutableList<String>
}

data class LenientHoldGroupRequiredLabelRule(var message: String) : LenientHoldGroupRule {
    override fun check(item: LenientHoldGroup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientHoldGroupRequiredCodeRule(var message: String) : LenientHoldGroupRule {
    override fun check(item: LenientHoldGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientHoldGroupRequiredCountryRule(var message: String) : LenientHoldGroupRule {
    override fun check(item: LenientHoldGroup): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientHoldGroupRequiredUnitRule(var message: String) : LenientHoldGroupRule {
    override fun check(item: LenientHoldGroup): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientHoldGroupAllowedLabelRule(var allowed: MutableList<String>) : LenientHoldGroupRule {
    override fun check(item: LenientHoldGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient hold group. */
data class LenientHoldGroupValidator(var rules: MutableList<LenientHoldGroupRule>) {
    fun validate(item: LenientHoldGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientHoldGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientHoldGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientHoldGroupValidator(): LenientHoldGroupValidator {
    return LenientHoldGroupValidator(
        mutableListOf<LenientHoldGroupRule>(
            LenientHoldGroupRequiredLabelRule("lenient hold group needs this field"),
            LenientHoldGroupRequiredCodeRule("lenient hold group needs an entry"),
            LenientHoldGroupRequiredCountryRule("lenient hold group needs this field"),
            LenientHoldGroupRequiredUnitRule("lenient hold group needs a value"),
            LenientHoldGroupAllowedLabelRule(mutableListOf<String>("regional", "legacy", "lenient")),
        ),
    )
}
