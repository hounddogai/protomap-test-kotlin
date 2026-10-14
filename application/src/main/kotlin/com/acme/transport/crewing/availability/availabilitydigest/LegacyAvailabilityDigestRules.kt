package com.acme.transport.crewing.availability.availabilitydigest

/** A rule a legacy availability digest must pass. Check returns its problems, or none. */
interface LegacyAvailabilityDigestRule {
    fun check(item: LegacyAvailabilityDigest): MutableList<String>
}

data class LegacyAvailabilityDigestCodePrefixRule(var prefix: String) : LegacyAvailabilityDigestRule {
    override fun check(item: LegacyAvailabilityDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAvailabilityDigestRequiredCodeRule(var message: String) : LegacyAvailabilityDigestRule {
    override fun check(item: LegacyAvailabilityDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyAvailabilityDigestContactEmailLengthRule(var maxLength: Int) : LegacyAvailabilityDigestRule {
    override fun check(item: LegacyAvailabilityDigest): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAvailabilityDigestWeightRangeRule(
    var min: Double,
    var max: Double,
) : LegacyAvailabilityDigestRule {
    override fun check(item: LegacyAvailabilityDigest): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAvailabilityDigestCodeLengthRule(var maxLength: Int) : LegacyAvailabilityDigestRule {
    override fun check(item: LegacyAvailabilityDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy availability digest. */
data class LegacyAvailabilityDigestValidator(var rules: MutableList<LegacyAvailabilityDigestRule>) {
    fun validate(item: LegacyAvailabilityDigest): MutableList<String> {
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

    fun requireValid(item: LegacyAvailabilityDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyAvailabilityDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyAvailabilityDigestValidator(): LegacyAvailabilityDigestValidator {
    return LegacyAvailabilityDigestValidator(
        mutableListOf<LegacyAvailabilityDigestRule>(
            LegacyAvailabilityDigestCodePrefixRule("X"),
            LegacyAvailabilityDigestRequiredCodeRule("legacy availability digest needs a value"),
            LegacyAvailabilityDigestContactEmailLengthRule(120),
            LegacyAvailabilityDigestWeightRangeRule(6.4, 1754.5),
            LegacyAvailabilityDigestCodeLengthRule(1000),
        ),
    )
}
