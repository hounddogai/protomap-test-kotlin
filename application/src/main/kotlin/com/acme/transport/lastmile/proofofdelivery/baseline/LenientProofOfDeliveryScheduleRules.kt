package com.acme.transport.lastmile.proofofdelivery.baseline

/** A rule a lenient proof of delivery schedule must pass. Check returns its problems, or none. */
interface LenientProofOfDeliveryScheduleRule {
    fun check(item: LenientProofOfDeliverySchedule): MutableList<String>
}

data class LenientProofOfDeliveryScheduleRequiredChecksumRule(
    var message: String,
) : LenientProofOfDeliveryScheduleRule {
    override fun check(item: LenientProofOfDeliverySchedule): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientProofOfDeliveryScheduleDiscountRangeRule(
    var min: Double,
    var max: Double,
) : LenientProofOfDeliveryScheduleRule {
    override fun check(item: LenientProofOfDeliverySchedule): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class LenientProofOfDeliveryScheduleTimezoneLengthRule(
    var maxLength: Int,
) : LenientProofOfDeliveryScheduleRule {
    override fun check(item: LenientProofOfDeliverySchedule): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientProofOfDeliveryScheduleRequiredCodeRule(
    var message: String,
) : LenientProofOfDeliveryScheduleRule {
    override fun check(item: LenientProofOfDeliverySchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientProofOfDeliveryScheduleCodePrefixRule(
    var prefix: String,
) : LenientProofOfDeliveryScheduleRule {
    override fun check(item: LenientProofOfDeliverySchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient proof of delivery schedule. */
data class LenientProofOfDeliveryScheduleValidator(var rules: MutableList<LenientProofOfDeliveryScheduleRule>) {
    fun validate(item: LenientProofOfDeliverySchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientProofOfDeliverySchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientProofOfDeliverySchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientProofOfDeliveryScheduleValidator(): LenientProofOfDeliveryScheduleValidator {
    return LenientProofOfDeliveryScheduleValidator(
        mutableListOf<LenientProofOfDeliveryScheduleRule>(
            LenientProofOfDeliveryScheduleRequiredChecksumRule(
                "lenient proof of delivery schedule needs a value",
            ),
            LenientProofOfDeliveryScheduleDiscountRangeRule(0.1, 2395.7),
            LenientProofOfDeliveryScheduleTimezoneLengthRule(64),
            LenientProofOfDeliveryScheduleRequiredCodeRule("lenient proof of delivery schedule needs a value"),
            LenientProofOfDeliveryScheduleCodePrefixRule("ACM"),
        ),
    )
}
