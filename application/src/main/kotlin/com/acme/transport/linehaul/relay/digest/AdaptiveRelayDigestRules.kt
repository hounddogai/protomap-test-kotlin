package com.acme.transport.linehaul.relay.digest

/** A rule a adaptive relay digest must pass. Check returns its problems, or none. */
interface AdaptiveRelayDigestRule {
    fun check(item: AdaptiveRelayDigest): MutableList<String>
}

data class AdaptiveRelayDigestRegionLengthRule(var maxLength: Int) : AdaptiveRelayDigestRule {
    override fun check(item: AdaptiveRelayDigest): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRelayDigestWindowDaysRangeRule(var min: Int, var max: Int) : AdaptiveRelayDigestRule {
    override fun check(item: AdaptiveRelayDigest): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class AdaptiveRelayDigestCodeLengthRule(var maxLength: Int) : AdaptiveRelayDigestRule {
    override fun check(item: AdaptiveRelayDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRelayDigestNameLengthRule(var maxLength: Int) : AdaptiveRelayDigestRule {
    override fun check(item: AdaptiveRelayDigest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRelayDigestLabelLengthRule(var maxLength: Int) : AdaptiveRelayDigestRule {
    override fun check(item: AdaptiveRelayDigest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive relay digest. */
data class AdaptiveRelayDigestValidator(var rules: MutableList<AdaptiveRelayDigestRule>) {
    fun validate(item: AdaptiveRelayDigest): MutableList<String> {
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

    fun requireValid(item: AdaptiveRelayDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveRelayDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveRelayDigestValidator(): AdaptiveRelayDigestValidator {
    return AdaptiveRelayDigestValidator(
        mutableListOf<AdaptiveRelayDigestRule>(
            AdaptiveRelayDigestRegionLengthRule(86400),
            AdaptiveRelayDigestWindowDaysRangeRule(255, 86400),
            AdaptiveRelayDigestCodeLengthRule(255),
            AdaptiveRelayDigestNameLengthRule(16),
            AdaptiveRelayDigestLabelLengthRule(255),
        ),
    )
}
