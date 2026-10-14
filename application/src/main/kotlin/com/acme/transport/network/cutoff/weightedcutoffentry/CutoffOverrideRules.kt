package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.text.normalize

/** A rule a cutoff override must pass. Check returns its problems, or none. */
interface CutoffOverrideRule {
    fun check(item: CutoffOverride): MutableList<String>
}

data class CutoffOverrideRequiredCodeRule(var message: String) : CutoffOverrideRule {
    override fun check(item: CutoffOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffOverrideAllowedUnitRule(var allowed: MutableList<String>) : CutoffOverrideRule {
    override fun check(item: CutoffOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class CutoffOverrideCodeLengthRule(var maxLength: Int) : CutoffOverrideRule {
    override fun check(item: CutoffOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff override. */
data class CutoffOverrideValidator(var rules: MutableList<CutoffOverrideRule>) {
    fun validate(item: CutoffOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CutoffOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffOverrideValidator(): CutoffOverrideValidator {
    return CutoffOverrideValidator(
        mutableListOf<CutoffOverrideRule>(
            CutoffOverrideRequiredCodeRule("cutoff override needs an entry"),
            CutoffOverrideAllowedUnitRule(mutableListOf<String>("strict", "capped", "rolling")),
            CutoffOverrideCodeLengthRule(64),
        ),
    )
}
