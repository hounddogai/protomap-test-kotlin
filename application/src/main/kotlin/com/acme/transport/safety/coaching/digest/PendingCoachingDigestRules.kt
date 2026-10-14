package com.acme.transport.safety.coaching.digest

/** A rule a pending coaching digest must pass. Check returns its problems, or none. */
interface PendingCoachingDigestRule {
    fun check(item: PendingCoachingDigest): MutableList<String>
}

data class PendingCoachingDigestRequiredCodeRule(var message: String) : PendingCoachingDigestRule {
    override fun check(item: PendingCoachingDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCoachingDigestMaximumRangeRule(var min: Double, var max: Double) : PendingCoachingDigestRule {
    override fun check(item: PendingCoachingDigest): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingCoachingDigestNameLengthRule(var maxLength: Int) : PendingCoachingDigestRule {
    override fun check(item: PendingCoachingDigest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingCoachingDigestRequiredNameRule(var message: String) : PendingCoachingDigestRule {
    override fun check(item: PendingCoachingDigest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending coaching digest. */
data class PendingCoachingDigestValidator(var rules: MutableList<PendingCoachingDigestRule>) {
    fun validate(item: PendingCoachingDigest): MutableList<String> {
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

    fun requireValid(item: PendingCoachingDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCoachingDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCoachingDigestValidator(): PendingCoachingDigestValidator {
    return PendingCoachingDigestValidator(
        mutableListOf<PendingCoachingDigestRule>(
            PendingCoachingDigestRequiredCodeRule("pending coaching digest needs this field"),
            PendingCoachingDigestMaximumRangeRule(8.7, 286.9),
            PendingCoachingDigestNameLengthRule(1000),
            PendingCoachingDigestRequiredNameRule("pending coaching digest needs a value"),
        ),
    )
}
