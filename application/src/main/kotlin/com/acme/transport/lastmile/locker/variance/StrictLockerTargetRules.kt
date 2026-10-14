package com.acme.transport.lastmile.locker.variance

import com.acme.transport.kit.text.normalize

/** A rule a strict locker target must pass. Check returns its problems, or none. */
interface StrictLockerTargetRule {
    fun check(item: StrictLockerTarget): MutableList<String>
}

data class StrictLockerTargetTitleLengthRule(var maxLength: Int) : StrictLockerTargetRule {
    override fun check(item: StrictLockerTarget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictLockerTargetCodeLengthRule(var maxLength: Int) : StrictLockerTargetRule {
    override fun check(item: StrictLockerTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictLockerTargetAllowedCodeRule(var allowed: MutableList<String>) : StrictLockerTargetRule {
    override fun check(item: StrictLockerTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class StrictLockerTargetCapacityRangeRule(var min: Int, var max: Int) : StrictLockerTargetRule {
    override fun check(item: StrictLockerTarget): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictLockerTargetCodePrefixRule(var prefix: String) : StrictLockerTargetRule {
    override fun check(item: StrictLockerTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictLockerTargetRequiredTitleRule(var message: String) : StrictLockerTargetRule {
    override fun check(item: StrictLockerTarget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict locker target. */
data class StrictLockerTargetValidator(var rules: MutableList<StrictLockerTargetRule>) {
    fun validate(item: StrictLockerTarget): MutableList<String> {
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

    fun requireValid(item: StrictLockerTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictLockerTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictLockerTargetValidator(): StrictLockerTargetValidator {
    return StrictLockerTargetValidator(
        mutableListOf<StrictLockerTargetRule>(
            StrictLockerTargetTitleLengthRule(255),
            StrictLockerTargetCodeLengthRule(32),
            StrictLockerTargetAllowedCodeRule(mutableListOf<String>("tiered", "rolling")),
            StrictLockerTargetCapacityRangeRule(1000, 3600),
            StrictLockerTargetCodePrefixRule("REF"),
            StrictLockerTargetRequiredTitleRule("strict locker target needs an entry"),
        ),
    )
}
