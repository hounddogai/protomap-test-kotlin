package com.acme.transport.customs.clearance.metric

import com.acme.transport.kit.text.normalize

/** A rule a clearance mapping must pass. Check returns its problems, or none. */
interface ClearanceMappingRule {
    fun check(item: ClearanceMapping): MutableList<String>
}

data class ClearanceMappingTitleLengthRule(var maxLength: Int) : ClearanceMappingRule {
    override fun check(item: ClearanceMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceMappingRequiredCarrierRule(var message: String) : ClearanceMappingRule {
    override fun check(item: ClearanceMapping): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceMappingAllowedTitleRule(var allowed: MutableList<String>) : ClearanceMappingRule {
    override fun check(item: ClearanceMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance mapping. */
data class ClearanceMappingValidator(var rules: MutableList<ClearanceMappingRule>) {
    fun validate(item: ClearanceMapping): MutableList<String> {
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

    fun requireValid(item: ClearanceMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceMappingValidator(): ClearanceMappingValidator {
    return ClearanceMappingValidator(
        mutableListOf<ClearanceMappingRule>(
            ClearanceMappingTitleLengthRule(3600),
            ClearanceMappingRequiredCarrierRule("clearance mapping needs a value"),
            ClearanceMappingAllowedTitleRule(mutableListOf<String>("seasonal")),
        ),
    )
}
