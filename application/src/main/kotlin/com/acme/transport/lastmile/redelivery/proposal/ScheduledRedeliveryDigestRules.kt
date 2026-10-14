package com.acme.transport.lastmile.redelivery.proposal

/** A rule a scheduled redelivery digest must pass. Check returns its problems, or none. */
interface ScheduledRedeliveryDigestRule {
    fun check(item: ScheduledRedeliveryDigest): MutableList<String>
}

data class ScheduledRedeliveryDigestTimezoneLengthRule(var maxLength: Int) : ScheduledRedeliveryDigestRule {
    override fun check(item: ScheduledRedeliveryDigest): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryDigestGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledRedeliveryDigestRule {
    override fun check(item: ScheduledRedeliveryDigest): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryDigestExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledRedeliveryDigestRule {
    override fun check(item: ScheduledRedeliveryDigest): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryDigestCodeLengthRule(var maxLength: Int) : ScheduledRedeliveryDigestRule {
    override fun check(item: ScheduledRedeliveryDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryDigestVersionRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledRedeliveryDigestRule {
    override fun check(item: ScheduledRedeliveryDigest): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled redelivery digest. */
data class ScheduledRedeliveryDigestValidator(var rules: MutableList<ScheduledRedeliveryDigestRule>) {
    fun validate(item: ScheduledRedeliveryDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledRedeliveryDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRedeliveryDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRedeliveryDigestValidator(): ScheduledRedeliveryDigestValidator {
    return ScheduledRedeliveryDigestValidator(
        mutableListOf<ScheduledRedeliveryDigestRule>(
            ScheduledRedeliveryDigestTimezoneLengthRule(120),
            ScheduledRedeliveryDigestGraceDaysRangeRule(64, 120),
            ScheduledRedeliveryDigestExpiresAtRangeRule(120, 120),
            ScheduledRedeliveryDigestCodeLengthRule(32),
            ScheduledRedeliveryDigestVersionRangeRule(120, 255),
        ),
    )
}
