package com.acme.transport.lastmile.parcel.plan

import com.acme.transport.kit.text.normalize

/** A rule a parcel plan must pass. Check returns its problems, or none. */
interface ParcelPlanRule {
    fun check(item: ParcelPlan): MutableList<String>
}

data class ParcelPlanCapacityRangeRule(var min: Int, var max: Int) : ParcelPlanRule {
    override fun check(item: ParcelPlan): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ParcelPlanShiftCodeLengthRule(var maxLength: Int) : ParcelPlanRule {
    override fun check(item: ParcelPlan): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelPlanAllowedCodeRule(var allowed: MutableList<String>) : ParcelPlanRule {
    override fun check(item: ParcelPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ParcelPlanRequiredCodeRule(var message: String) : ParcelPlanRule {
    override fun check(item: ParcelPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelPlanRequiredNameRule(var message: String) : ParcelPlanRule {
    override fun check(item: ParcelPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelPlanCodePrefixRule(var prefix: String) : ParcelPlanRule {
    override fun check(item: ParcelPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel plan. */
data class ParcelPlanValidator(var rules: MutableList<ParcelPlanRule>) {
    fun validate(item: ParcelPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelPlanValidator(): ParcelPlanValidator {
    return ParcelPlanValidator(
        mutableListOf<ParcelPlanRule>(
            ParcelPlanCapacityRangeRule(1000, 3600),
            ParcelPlanShiftCodeLengthRule(86400),
            ParcelPlanAllowedCodeRule(mutableListOf<String>("scheduled", "weighted")),
            ParcelPlanRequiredCodeRule("parcel plan needs an entry"),
            ParcelPlanRequiredNameRule("parcel plan needs an entry"),
            ParcelPlanCodePrefixRule("ACM"),
        ),
    )
}
