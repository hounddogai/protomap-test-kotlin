package com.acme.transport.network.cutoff.cutoffclaim

/** A rule a default cutoff claim must pass. Check returns its problems, or none. */
interface DefaultCutoffClaimRule {
    fun check(item: DefaultCutoffClaim): MutableList<String>
}

data class DefaultCutoffClaimOdometerRangeRule(var min: Int, var max: Int) : DefaultCutoffClaimRule {
    override fun check(item: DefaultCutoffClaim): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffClaimMarginRangeRule(var min: Double, var max: Double) : DefaultCutoffClaimRule {
    override fun check(item: DefaultCutoffClaim): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffClaimNameLengthRule(var maxLength: Int) : DefaultCutoffClaimRule {
    override fun check(item: DefaultCutoffClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffClaimLaneLengthRule(var maxLength: Int) : DefaultCutoffClaimRule {
    override fun check(item: DefaultCutoffClaim): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default cutoff claim. */
data class DefaultCutoffClaimValidator(var rules: MutableList<DefaultCutoffClaimRule>) {
    fun validate(item: DefaultCutoffClaim): MutableList<String> {
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

    fun requireValid(item: DefaultCutoffClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCutoffClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCutoffClaimValidator(): DefaultCutoffClaimValidator {
    return DefaultCutoffClaimValidator(
        mutableListOf<DefaultCutoffClaimRule>(
            DefaultCutoffClaimOdometerRangeRule(80, 1000),
            DefaultCutoffClaimMarginRangeRule(6.7, 4809.7),
            DefaultCutoffClaimNameLengthRule(1000),
            DefaultCutoffClaimLaneLengthRule(255),
        ),
    )
}
