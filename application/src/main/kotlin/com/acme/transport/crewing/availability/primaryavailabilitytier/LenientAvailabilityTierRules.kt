package com.acme.transport.crewing.availability.primaryavailabilitytier

/** A rule a lenient availability tier must pass. Check returns its problems, or none. */
interface LenientAvailabilityTierRule {
    fun check(item: LenientAvailabilityTier): MutableList<String>
}

data class LenientAvailabilityTierRequiredNameRule(var message: String) : LenientAvailabilityTierRule {
    override fun check(item: LenientAvailabilityTier): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAvailabilityTierNameLengthRule(var maxLength: Int) : LenientAvailabilityTierRule {
    override fun check(item: LenientAvailabilityTier): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAvailabilityTierTitleLengthRule(var maxLength: Int) : LenientAvailabilityTierRule {
    override fun check(item: LenientAvailabilityTier): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient availability tier. */
data class LenientAvailabilityTierValidator(var rules: MutableList<LenientAvailabilityTierRule>) {
    fun validate(item: LenientAvailabilityTier): MutableList<String> {
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

    fun requireValid(item: LenientAvailabilityTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientAvailabilityTier>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientAvailabilityTierValidator(): LenientAvailabilityTierValidator {
    return LenientAvailabilityTierValidator(
        mutableListOf<LenientAvailabilityTierRule>(
            LenientAvailabilityTierRequiredNameRule("lenient availability tier needs an entry"),
            LenientAvailabilityTierNameLengthRule(120),
            LenientAvailabilityTierTitleLengthRule(120),
        ),
    )
}
