package com.acme.transport.safety.violation.estimate

/** A rule a violation digest must pass. Check returns its problems, or none. */
interface ViolationDigestRule {
    fun check(item: ViolationDigest): MutableList<String>
}

data class ViolationDigestRequiredRegionRule(var message: String) : ViolationDigestRule {
    override fun check(item: ViolationDigest): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationDigestTimezoneLengthRule(var maxLength: Int) : ViolationDigestRule {
    override fun check(item: ViolationDigest): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationDigestCodeLengthRule(var maxLength: Int) : ViolationDigestRule {
    override fun check(item: ViolationDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationDigestWindowDaysRangeRule(var min: Int, var max: Int) : ViolationDigestRule {
    override fun check(item: ViolationDigest): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ViolationDigestRegionLengthRule(var maxLength: Int) : ViolationDigestRule {
    override fun check(item: ViolationDigest): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation digest. */
data class ViolationDigestValidator(var rules: MutableList<ViolationDigestRule>) {
    fun validate(item: ViolationDigest): MutableList<String> {
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

    fun requireValid(item: ViolationDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationDigestValidator(): ViolationDigestValidator {
    return ViolationDigestValidator(
        mutableListOf<ViolationDigestRule>(
            ViolationDigestRequiredRegionRule("violation digest needs a value"),
            ViolationDigestTimezoneLengthRule(86400),
            ViolationDigestCodeLengthRule(86400),
            ViolationDigestWindowDaysRangeRule(120, 3600),
            ViolationDigestRegionLengthRule(3600),
        ),
    )
}
