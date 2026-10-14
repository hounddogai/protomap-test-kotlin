package com.acme.transport.safety.violation.estimate

/** A rule a violation estimate must pass. Check returns its problems, or none. */
interface ViolationEstimateRule {
    fun check(item: ViolationEstimate): MutableList<String>
}

data class ViolationEstimateCodeLengthRule(var maxLength: Int) : ViolationEstimateRule {
    override fun check(item: ViolationEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationEstimateQuantityRangeRule(var min: Int, var max: Int) : ViolationEstimateRule {
    override fun check(item: ViolationEstimate): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class ViolationEstimateEtaMinutesRangeRule(var min: Int, var max: Int) : ViolationEstimateRule {
    override fun check(item: ViolationEstimate): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ViolationEstimateRegionLengthRule(var maxLength: Int) : ViolationEstimateRule {
    override fun check(item: ViolationEstimate): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationEstimateRequiredNameRule(var message: String) : ViolationEstimateRule {
    override fun check(item: ViolationEstimate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationEstimatePriorityRangeRule(var min: Int, var max: Int) : ViolationEstimateRule {
    override fun check(item: ViolationEstimate): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a violation estimate. */
data class ViolationEstimateValidator(var rules: MutableList<ViolationEstimateRule>) {
    fun validate(item: ViolationEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationEstimateValidator(): ViolationEstimateValidator {
    return ViolationEstimateValidator(
        mutableListOf<ViolationEstimateRule>(
            ViolationEstimateCodeLengthRule(120),
            ViolationEstimateQuantityRangeRule(255, 3600),
            ViolationEstimateEtaMinutesRangeRule(80, 1000),
            ViolationEstimateRegionLengthRule(32),
            ViolationEstimateRequiredNameRule("violation estimate needs a value"),
            ViolationEstimatePriorityRangeRule(120, 255),
        ),
    )
}
