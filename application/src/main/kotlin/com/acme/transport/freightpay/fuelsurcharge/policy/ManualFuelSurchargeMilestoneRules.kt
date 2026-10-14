package com.acme.transport.freightpay.fuelsurcharge.policy

/** A rule a manual fuel surcharge milestone must pass. Check returns its problems, or none. */
interface ManualFuelSurchargeMilestoneRule {
    fun check(item: ManualFuelSurchargeMilestone): MutableList<String>
}

data class ManualFuelSurchargeMilestoneCodePrefixRule(var prefix: String) : ManualFuelSurchargeMilestoneRule {
    override fun check(item: ManualFuelSurchargeMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelSurchargeMilestoneRequiredTitleRule(
    var message: String,
) : ManualFuelSurchargeMilestoneRule {
    override fun check(item: ManualFuelSurchargeMilestone): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelSurchargeMilestoneLimitRangeRule(
    var min: Int,
    var max: Int,
) : ManualFuelSurchargeMilestoneRule {
    override fun check(item: ManualFuelSurchargeMilestone): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelSurchargeMilestoneMarginRangeRule(
    var min: Double,
    var max: Double,
) : ManualFuelSurchargeMilestoneRule {
    override fun check(item: ManualFuelSurchargeMilestone): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual fuel surcharge milestone. */
data class ManualFuelSurchargeMilestoneValidator(var rules: MutableList<ManualFuelSurchargeMilestoneRule>) {
    fun validate(item: ManualFuelSurchargeMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualFuelSurchargeMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualFuelSurchargeMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultManualFuelSurchargeMilestoneValidator(): ManualFuelSurchargeMilestoneValidator {
    return ManualFuelSurchargeMilestoneValidator(
        mutableListOf<ManualFuelSurchargeMilestoneRule>(
            ManualFuelSurchargeMilestoneCodePrefixRule("Q"),
            ManualFuelSurchargeMilestoneRequiredTitleRule("manual fuel surcharge milestone needs a value"),
            ManualFuelSurchargeMilestoneLimitRangeRule(120, 255),
            ManualFuelSurchargeMilestoneMarginRangeRule(4.5, 4138.9),
        ),
    )
}
