package com.acme.transport.safety.violation.digest

import com.acme.transport.kit.text.normalize

/** A rule a pending violation digest must pass. Check returns its problems, or none. */
interface PendingViolationDigestRule {
    fun check(item: PendingViolationDigest): MutableList<String>
}

data class PendingViolationDigestAttemptsRangeRule(var min: Int, var max: Int) : PendingViolationDigestRule {
    override fun check(item: PendingViolationDigest): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingViolationDigestAllowedChannelRule(
    var allowed: MutableList<String>,
) : PendingViolationDigestRule {
    override fun check(item: PendingViolationDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class PendingViolationDigestFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : PendingViolationDigestRule {
    override fun check(item: PendingViolationDigest): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingViolationDigestRequiredCodeRule(var message: String) : PendingViolationDigestRule {
    override fun check(item: PendingViolationDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending violation digest. */
data class PendingViolationDigestValidator(var rules: MutableList<PendingViolationDigestRule>) {
    fun validate(item: PendingViolationDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingViolationDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingViolationDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingViolationDigestValidator(): PendingViolationDigestValidator {
    return PendingViolationDigestValidator(
        mutableListOf<PendingViolationDigestRule>(
            PendingViolationDigestAttemptsRangeRule(16, 32),
            PendingViolationDigestAllowedChannelRule(mutableListOf<String>("regional")),
            PendingViolationDigestFuelLitersRangeRule(5.4, 3415.4),
            PendingViolationDigestRequiredCodeRule("pending violation digest needs an entry"),
        ),
    )
}
