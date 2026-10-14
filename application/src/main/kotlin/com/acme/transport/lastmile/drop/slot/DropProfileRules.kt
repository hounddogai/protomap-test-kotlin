package com.acme.transport.lastmile.drop.slot

import com.acme.transport.kit.text.normalize

/** A rule a drop profile must pass. Check returns its problems, or none. */
interface DropProfileRule {
    fun check(item: DropProfile): MutableList<String>
}

data class DropProfileActiveGuardRule(var limit: Int) : DropProfileRule {
    override fun check(item: DropProfile): MutableList<String> {
        if (item.active && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class DropProfileAllowedCodeRule(var allowed: MutableList<String>) : DropProfileRule {
    override fun check(item: DropProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class DropProfileRequiredCurrencyRule(var message: String) : DropProfileRule {
    override fun check(item: DropProfile): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop profile. */
data class DropProfileValidator(var rules: MutableList<DropProfileRule>) {
    fun validate(item: DropProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DropProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultDropProfileValidator(): DropProfileValidator {
    return DropProfileValidator(
        mutableListOf<DropProfileRule>(
            DropProfileActiveGuardRule(32),
            DropProfileAllowedCodeRule(mutableListOf<String>()),
            DropProfileRequiredCurrencyRule("drop profile needs this field"),
        ),
    )
}
