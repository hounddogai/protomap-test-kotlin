package com.acme.transport.customs.clearance.milestone

import com.acme.transport.kit.text.normalize

/** A rule a clearance group must pass. Check returns its problems, or none. */
interface ClearanceGroupRule {
    fun check(item: ClearanceGroup): MutableList<String>
}

data class ClearanceGroupAllowedExternalRefRule(var allowed: MutableList<String>) : ClearanceGroupRule {
    override fun check(item: ClearanceGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceGroupRequiredExternalRefRule(var message: String) : ClearanceGroupRule {
    override fun check(item: ClearanceGroup): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceGroupCodeLengthRule(var maxLength: Int) : ClearanceGroupRule {
    override fun check(item: ClearanceGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceGroupDiscountRangeRule(var min: Double, var max: Double) : ClearanceGroupRule {
    override fun check(item: ClearanceGroup): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class ClearanceGroupRequiredDescriptionRule(var message: String) : ClearanceGroupRule {
    override fun check(item: ClearanceGroup): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance group. */
data class ClearanceGroupValidator(var rules: MutableList<ClearanceGroupRule>) {
    fun validate(item: ClearanceGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearanceGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceGroupValidator(): ClearanceGroupValidator {
    return ClearanceGroupValidator(
        mutableListOf<ClearanceGroupRule>(
            ClearanceGroupAllowedExternalRefRule(mutableListOf<String>("capped")),
            ClearanceGroupRequiredExternalRefRule("clearance group needs this field"),
            ClearanceGroupCodeLengthRule(1000),
            ClearanceGroupDiscountRangeRule(0.9, 928.8),
            ClearanceGroupRequiredDescriptionRule("clearance group needs this field"),
        ),
    )
}
