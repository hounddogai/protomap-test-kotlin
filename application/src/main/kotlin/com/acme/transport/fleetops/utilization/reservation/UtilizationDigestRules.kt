package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.kit.text.normalize

/** A rule a utilization digest must pass. Check returns its problems, or none. */
interface UtilizationDigestRule {
    fun check(item: UtilizationDigest): MutableList<String>
}

data class UtilizationDigestAmountRangeRule(var min: Double, var max: Double) : UtilizationDigestRule {
    override fun check(item: UtilizationDigest): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationDigestAllowedTrailerIdRule(var allowed: MutableList<String>) : UtilizationDigestRule {
    override fun check(item: UtilizationDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationDigestRequiredCodeRule(var message: String) : UtilizationDigestRule {
    override fun check(item: UtilizationDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationDigestTitleLengthRule(var maxLength: Int) : UtilizationDigestRule {
    override fun check(item: UtilizationDigest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization digest. */
data class UtilizationDigestValidator(var rules: MutableList<UtilizationDigestRule>) {
    fun validate(item: UtilizationDigest): MutableList<String> {
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

    fun requireValid(item: UtilizationDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationDigestValidator(): UtilizationDigestValidator {
    return UtilizationDigestValidator(
        mutableListOf<UtilizationDigestRule>(
            UtilizationDigestAmountRangeRule(3.3, 4328.2),
            UtilizationDigestAllowedTrailerIdRule(mutableListOf<String>("global", "primary", "weighted")),
            UtilizationDigestRequiredCodeRule("utilization digest needs a value"),
            UtilizationDigestTitleLengthRule(120),
        ),
    )
}
