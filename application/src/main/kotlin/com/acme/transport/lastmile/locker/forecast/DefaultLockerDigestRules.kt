package com.acme.transport.lastmile.locker.forecast

/** A rule a default locker digest must pass. Check returns its problems, or none. */
interface DefaultLockerDigestRule {
    fun check(item: DefaultLockerDigest): MutableList<String>
}

data class DefaultLockerDigestMinimumRangeRule(var min: Double, var max: Double) : DefaultLockerDigestRule {
    override fun check(item: DefaultLockerDigest): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class DefaultLockerDigestEndsAtRangeRule(var min: Int, var max: Int) : DefaultLockerDigestRule {
    override fun check(item: DefaultLockerDigest): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultLockerDigestRequiredLocaleRule(var message: String) : DefaultLockerDigestRule {
    override fun check(item: DefaultLockerDigest): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultLockerDigestPinnedGuardRule(var limit: Int) : DefaultLockerDigestRule {
    override fun check(item: DefaultLockerDigest): MutableList<String> {
        if (item.pinned && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class DefaultLockerDigestRequiredLabelRule(var message: String) : DefaultLockerDigestRule {
    override fun check(item: DefaultLockerDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultLockerDigestLabelLengthRule(var maxLength: Int) : DefaultLockerDigestRule {
    override fun check(item: DefaultLockerDigest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default locker digest. */
data class DefaultLockerDigestValidator(var rules: MutableList<DefaultLockerDigestRule>) {
    fun validate(item: DefaultLockerDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultLockerDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultLockerDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultLockerDigestValidator(): DefaultLockerDigestValidator {
    return DefaultLockerDigestValidator(
        mutableListOf<DefaultLockerDigestRule>(
            DefaultLockerDigestMinimumRangeRule(5.8, 1087.7),
            DefaultLockerDigestEndsAtRangeRule(16, 120),
            DefaultLockerDigestRequiredLocaleRule("default locker digest needs a value"),
            DefaultLockerDigestPinnedGuardRule(255),
            DefaultLockerDigestRequiredLabelRule("default locker digest needs a value"),
            DefaultLockerDigestLabelLengthRule(64),
        ),
    )
}
