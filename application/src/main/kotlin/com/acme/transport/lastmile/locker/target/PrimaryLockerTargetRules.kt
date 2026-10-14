package com.acme.transport.lastmile.locker.target

/** A rule a primary locker target must pass. Check returns its problems, or none. */
interface PrimaryLockerTargetRule {
    fun check(item: PrimaryLockerTarget): MutableList<String>
}

data class PrimaryLockerTargetRequiredCarrierRule(var message: String) : PrimaryLockerTargetRule {
    override fun check(item: PrimaryLockerTarget): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLockerTargetScoreRangeRule(var min: Double, var max: Double) : PrimaryLockerTargetRule {
    override fun check(item: PrimaryLockerTarget): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class PrimaryLockerTargetCodePrefixRule(var prefix: String) : PrimaryLockerTargetRule {
    override fun check(item: PrimaryLockerTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLockerTargetLabelLengthRule(var maxLength: Int) : PrimaryLockerTargetRule {
    override fun check(item: PrimaryLockerTarget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLockerTargetRequiredCodeRule(var message: String) : PrimaryLockerTargetRule {
    override fun check(item: PrimaryLockerTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary locker target. */
data class PrimaryLockerTargetValidator(var rules: MutableList<PrimaryLockerTargetRule>) {
    fun validate(item: PrimaryLockerTarget): MutableList<String> {
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

    fun requireValid(item: PrimaryLockerTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryLockerTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryLockerTargetValidator(): PrimaryLockerTargetValidator {
    return PrimaryLockerTargetValidator(
        mutableListOf<PrimaryLockerTargetRule>(
            PrimaryLockerTargetRequiredCarrierRule("primary locker target needs a value"),
            PrimaryLockerTargetScoreRangeRule(1.8, 987.5),
            PrimaryLockerTargetCodePrefixRule("X"),
            PrimaryLockerTargetLabelLengthRule(3600),
            PrimaryLockerTargetRequiredCodeRule("primary locker target needs an entry"),
        ),
    )
}
