package com.acme.transport.linehaul.tender.bundle

/** A rule a rolling tender group must pass. Check returns its problems, or none. */
interface RollingTenderGroupRule {
    fun check(item: RollingTenderGroup): MutableList<String>
}

data class RollingTenderGroupRateRangeRule(var min: Double, var max: Double) : RollingTenderGroupRule {
    override fun check(item: RollingTenderGroup): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class RollingTenderGroupCostRangeRule(var min: Double, var max: Double) : RollingTenderGroupRule {
    override fun check(item: RollingTenderGroup): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class RollingTenderGroupNameLengthRule(var maxLength: Int) : RollingTenderGroupRule {
    override fun check(item: RollingTenderGroup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingTenderGroupRequiredReviewerRule(var message: String) : RollingTenderGroupRule {
    override fun check(item: RollingTenderGroup): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingTenderGroupReviewerLengthRule(var maxLength: Int) : RollingTenderGroupRule {
    override fun check(item: RollingTenderGroup): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling tender group. */
data class RollingTenderGroupValidator(var rules: MutableList<RollingTenderGroupRule>) {
    fun validate(item: RollingTenderGroup): MutableList<String> {
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

    fun requireValid(item: RollingTenderGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingTenderGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingTenderGroupValidator(): RollingTenderGroupValidator {
    return RollingTenderGroupValidator(
        mutableListOf<RollingTenderGroupRule>(
            RollingTenderGroupRateRangeRule(5.2, 2688.5),
            RollingTenderGroupCostRangeRule(1.0, 4055.8),
            RollingTenderGroupNameLengthRule(1000),
            RollingTenderGroupRequiredReviewerRule("rolling tender group needs a value"),
            RollingTenderGroupReviewerLengthRule(255),
        ),
    )
}
