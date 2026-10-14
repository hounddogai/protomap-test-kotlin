package com.acme.transport.linehaul.tender.forecast

/** A rule a legacy tender profile must pass. Check returns its problems, or none. */
interface LegacyTenderProfileRule {
    fun check(item: LegacyTenderProfile): MutableList<String>
}

data class LegacyTenderProfileRegionLengthRule(var maxLength: Int) : LegacyTenderProfileRule {
    override fun check(item: LegacyTenderProfile): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTenderProfileCodePrefixRule(var prefix: String) : LegacyTenderProfileRule {
    override fun check(item: LegacyTenderProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTenderProfileAttemptsRangeRule(var min: Int, var max: Int) : LegacyTenderProfileRule {
    override fun check(item: LegacyTenderProfile): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class LegacyTenderProfileSummaryLengthRule(var maxLength: Int) : LegacyTenderProfileRule {
    override fun check(item: LegacyTenderProfile): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTenderProfileRequiredRegionRule(var message: String) : LegacyTenderProfileRule {
    override fun check(item: LegacyTenderProfile): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy tender profile. */
data class LegacyTenderProfileValidator(var rules: MutableList<LegacyTenderProfileRule>) {
    fun validate(item: LegacyTenderProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyTenderProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTenderProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTenderProfileValidator(): LegacyTenderProfileValidator {
    return LegacyTenderProfileValidator(
        mutableListOf<LegacyTenderProfileRule>(
            LegacyTenderProfileRegionLengthRule(80),
            LegacyTenderProfileCodePrefixRule("ACM"),
            LegacyTenderProfileAttemptsRangeRule(16, 3600),
            LegacyTenderProfileSummaryLengthRule(255),
            LegacyTenderProfileRequiredRegionRule("legacy tender profile needs an entry"),
        ),
    )
}
