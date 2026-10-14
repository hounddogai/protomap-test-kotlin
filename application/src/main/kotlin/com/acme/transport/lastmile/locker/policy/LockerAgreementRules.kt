package com.acme.transport.lastmile.locker.policy

import com.acme.transport.kit.text.normalize

/** A rule a locker agreement must pass. Check returns its problems, or none. */
interface LockerAgreementRule {
    fun check(item: LockerAgreement): MutableList<String>
}

data class LockerAgreementThresholdRangeRule(var min: Double, var max: Double) : LockerAgreementRule {
    override fun check(item: LockerAgreement): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class LockerAgreementRequiredTrailerIdRule(var message: String) : LockerAgreementRule {
    override fun check(item: LockerAgreement): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerAgreementVisibleGuardRule(var limit: Double) : LockerAgreementRule {
    override fun check(item: LockerAgreement): MutableList<String> {
        if (item.visible && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class LockerAgreementLaneLengthRule(var maxLength: Int) : LockerAgreementRule {
    override fun check(item: LockerAgreement): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerAgreementRequiredCodeRule(var message: String) : LockerAgreementRule {
    override fun check(item: LockerAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerAgreementAllowedTrailerIdRule(var allowed: MutableList<String>) : LockerAgreementRule {
    override fun check(item: LockerAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker agreement. */
data class LockerAgreementValidator(var rules: MutableList<LockerAgreementRule>) {
    fun validate(item: LockerAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LockerAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerAgreementValidator(): LockerAgreementValidator {
    return LockerAgreementValidator(
        mutableListOf<LockerAgreementRule>(
            LockerAgreementThresholdRangeRule(8.5, 4361.1),
            LockerAgreementRequiredTrailerIdRule("locker agreement needs an entry"),
            LockerAgreementVisibleGuardRule(7.4),
            LockerAgreementLaneLengthRule(1000),
            LockerAgreementRequiredCodeRule("locker agreement needs this field"),
            LockerAgreementAllowedTrailerIdRule(mutableListOf<String>("legacy", "adaptive", "regional")),
        ),
    )
}
