package com.acme.transport.linehaul.laneplan.exception

/** A rule a lane plan estimate must pass. Check returns its problems, or none. */
interface LanePlanEstimateRule {
    fun check(item: LanePlanEstimate): MutableList<String>
}

data class LanePlanEstimateLimitRangeRule(var min: Int, var max: Int) : LanePlanEstimateRule {
    override fun check(item: LanePlanEstimate): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class LanePlanEstimateRequiredLabelRule(var message: String) : LanePlanEstimateRule {
    override fun check(item: LanePlanEstimate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanEstimatePhoneLengthRule(var maxLength: Int) : LanePlanEstimateRule {
    override fun check(item: LanePlanEstimate): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanEstimateLabelLengthRule(var maxLength: Int) : LanePlanEstimateRule {
    override fun check(item: LanePlanEstimate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan estimate. */
data class LanePlanEstimateValidator(var rules: MutableList<LanePlanEstimateRule>) {
    fun validate(item: LanePlanEstimate): MutableList<String> {
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

    fun requireValid(item: LanePlanEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanEstimateValidator(): LanePlanEstimateValidator {
    return LanePlanEstimateValidator(
        mutableListOf<LanePlanEstimateRule>(
            LanePlanEstimateLimitRangeRule(80, 255),
            LanePlanEstimateRequiredLabelRule("lane plan estimate needs an entry"),
            LanePlanEstimatePhoneLengthRule(120),
            LanePlanEstimateLabelLengthRule(80),
        ),
    )
}
