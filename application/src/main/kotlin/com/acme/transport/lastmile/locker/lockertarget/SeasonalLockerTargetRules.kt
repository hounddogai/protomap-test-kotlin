package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.kit.text.normalize

/** A rule a seasonal locker target must pass. Check returns its problems, or none. */
interface SeasonalLockerTargetRule {
    fun check(item: SeasonalLockerTarget): MutableList<String>
}

data class SeasonalLockerTargetRequiredCodeRule(var message: String) : SeasonalLockerTargetRule {
    override fun check(item: SeasonalLockerTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLockerTargetRequiredChecksumRule(var message: String) : SeasonalLockerTargetRule {
    override fun check(item: SeasonalLockerTarget): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLockerTargetRetriesRangeRule(var min: Int, var max: Int) : SeasonalLockerTargetRule {
    override fun check(item: SeasonalLockerTarget): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class SeasonalLockerTargetCodeLengthRule(var maxLength: Int) : SeasonalLockerTargetRule {
    override fun check(item: SeasonalLockerTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLockerTargetCodePrefixRule(var prefix: String) : SeasonalLockerTargetRule {
    override fun check(item: SeasonalLockerTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLockerTargetAllowedChecksumRule(
    var allowed: MutableList<String>,
) : SeasonalLockerTargetRule {
    override fun check(item: SeasonalLockerTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal locker target. */
data class SeasonalLockerTargetValidator(var rules: MutableList<SeasonalLockerTargetRule>) {
    fun validate(item: SeasonalLockerTarget): MutableList<String> {
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

    fun requireValid(item: SeasonalLockerTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalLockerTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalLockerTargetValidator(): SeasonalLockerTargetValidator {
    return SeasonalLockerTargetValidator(
        mutableListOf<SeasonalLockerTargetRule>(
            SeasonalLockerTargetRequiredCodeRule("seasonal locker target needs an entry"),
            SeasonalLockerTargetRequiredChecksumRule("seasonal locker target needs a value"),
            SeasonalLockerTargetRetriesRangeRule(1000, 3600),
            SeasonalLockerTargetCodeLengthRule(3600),
            SeasonalLockerTargetCodePrefixRule("Q"),
            SeasonalLockerTargetAllowedChecksumRule(mutableListOf<String>("lenient", "pending")),
        ),
    )
}
