package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.text.normalize

/** A rule a legacy near miss plan must pass. Check returns its problems, or none. */
interface LegacyNearMissPlanRule {
    fun check(item: LegacyNearMissPlan): MutableList<String>
}

data class LegacyNearMissPlanRequiredExternalRefRule(var message: String) : LegacyNearMissPlanRule {
    override fun check(item: LegacyNearMissPlan): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyNearMissPlanExternalRefLengthRule(var maxLength: Int) : LegacyNearMissPlanRule {
    override fun check(item: LegacyNearMissPlan): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyNearMissPlanAllowedExternalRefRule(var allowed: MutableList<String>) : LegacyNearMissPlanRule {
    override fun check(item: LegacyNearMissPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class LegacyNearMissPlanTrailerIdLengthRule(var maxLength: Int) : LegacyNearMissPlanRule {
    override fun check(item: LegacyNearMissPlan): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy near miss plan. */
data class LegacyNearMissPlanValidator(var rules: MutableList<LegacyNearMissPlanRule>) {
    fun validate(item: LegacyNearMissPlan): MutableList<String> {
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

    fun requireValid(item: LegacyNearMissPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyNearMissPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyNearMissPlanValidator(): LegacyNearMissPlanValidator {
    return LegacyNearMissPlanValidator(
        mutableListOf<LegacyNearMissPlanRule>(
            LegacyNearMissPlanRequiredExternalRefRule("legacy near miss plan needs this field"),
            LegacyNearMissPlanExternalRefLengthRule(64),
            LegacyNearMissPlanAllowedExternalRefRule(mutableListOf<String>("manual")),
            LegacyNearMissPlanTrailerIdLengthRule(64),
        ),
    )
}
