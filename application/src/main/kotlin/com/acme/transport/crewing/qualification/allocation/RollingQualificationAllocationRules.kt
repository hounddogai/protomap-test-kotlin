package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.kit.text.normalize

/** A rule a rolling qualification allocation must pass. Check returns its problems, or none. */
interface RollingQualificationAllocationRule {
    fun check(item: RollingQualificationAllocation): MutableList<String>
}

data class RollingQualificationAllocationRequiredCodeRule(
    var message: String,
) : RollingQualificationAllocationRule {
    override fun check(item: RollingQualificationAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingQualificationAllocationTitleLengthRule(
    var maxLength: Int,
) : RollingQualificationAllocationRule {
    override fun check(item: RollingQualificationAllocation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingQualificationAllocationAllowedCodeRule(
    var allowed: MutableList<String>,
) : RollingQualificationAllocationRule {
    override fun check(item: RollingQualificationAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class RollingQualificationAllocationBudgetRangeRule(
    var min: Double,
    var max: Double,
) : RollingQualificationAllocationRule {
    override fun check(item: RollingQualificationAllocation): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class RollingQualificationAllocationRequiredContactEmailRule(
    var message: String,
) : RollingQualificationAllocationRule {
    override fun check(item: RollingQualificationAllocation): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling qualification allocation. */
data class RollingQualificationAllocationValidator(var rules: MutableList<RollingQualificationAllocationRule>) {
    fun validate(item: RollingQualificationAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingQualificationAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingQualificationAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingQualificationAllocationValidator(): RollingQualificationAllocationValidator {
    return RollingQualificationAllocationValidator(
        mutableListOf<RollingQualificationAllocationRule>(
            RollingQualificationAllocationRequiredCodeRule("rolling qualification allocation needs a value"),
            RollingQualificationAllocationTitleLengthRule(32),
            RollingQualificationAllocationAllowedCodeRule(mutableListOf<String>("lenient")),
            RollingQualificationAllocationBudgetRangeRule(8.1, 2769.0),
            RollingQualificationAllocationRequiredContactEmailRule(
                "rolling qualification allocation needs an entry",
            ),
        ),
    )
}
