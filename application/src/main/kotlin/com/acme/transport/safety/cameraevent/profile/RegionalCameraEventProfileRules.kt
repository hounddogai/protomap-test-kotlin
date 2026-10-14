package com.acme.transport.safety.cameraevent.profile

/** A rule a regional camera event profile must pass. Check returns its problems, or none. */
interface RegionalCameraEventProfileRule {
    fun check(item: RegionalCameraEventProfile): MutableList<String>
}

data class RegionalCameraEventProfileEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : RegionalCameraEventProfileRule {
    override fun check(item: RegionalCameraEventProfile): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class RegionalCameraEventProfilePhoneLengthRule(var maxLength: Int) : RegionalCameraEventProfileRule {
    override fun check(item: RegionalCameraEventProfile): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCameraEventProfileCodePrefixRule(var prefix: String) : RegionalCameraEventProfileRule {
    override fun check(item: RegionalCameraEventProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCameraEventProfileCeilingRangeRule(
    var min: Double,
    var max: Double,
) : RegionalCameraEventProfileRule {
    override fun check(item: RegionalCameraEventProfile): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class RegionalCameraEventProfileRequiredPhoneRule(var message: String) : RegionalCameraEventProfileRule {
    override fun check(item: RegionalCameraEventProfile): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCameraEventProfileTitleLengthRule(var maxLength: Int) : RegionalCameraEventProfileRule {
    override fun check(item: RegionalCameraEventProfile): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional camera event profile. */
data class RegionalCameraEventProfileValidator(var rules: MutableList<RegionalCameraEventProfileRule>) {
    fun validate(item: RegionalCameraEventProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalCameraEventProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCameraEventProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCameraEventProfileValidator(): RegionalCameraEventProfileValidator {
    return RegionalCameraEventProfileValidator(
        mutableListOf<RegionalCameraEventProfileRule>(
            RegionalCameraEventProfileEndsAtRangeRule(32, 1000),
            RegionalCameraEventProfilePhoneLengthRule(86400),
            RegionalCameraEventProfileCodePrefixRule("REF"),
            RegionalCameraEventProfileCeilingRangeRule(5.9, 1286.7),
            RegionalCameraEventProfileRequiredPhoneRule("regional camera event profile needs an entry"),
            RegionalCameraEventProfileTitleLengthRule(120),
        ),
    )
}
