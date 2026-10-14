package com.acme.transport.fleetops.tire.recorditem

/** A rule a legacy tire digest must pass. Check returns its problems, or none. */
interface LegacyTireDigestRule {
    fun check(item: LegacyTireDigest): MutableList<String>
}

data class LegacyTireDigestDriverIdLengthRule(var maxLength: Int) : LegacyTireDigestRule {
    override fun check(item: LegacyTireDigest): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireDigestRequiredCodeRule(var message: String) : LegacyTireDigestRule {
    override fun check(item: LegacyTireDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireDigestDescriptionLengthRule(var maxLength: Int) : LegacyTireDigestRule {
    override fun check(item: LegacyTireDigest): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy tire digest. */
data class LegacyTireDigestValidator(var rules: MutableList<LegacyTireDigestRule>) {
    fun validate(item: LegacyTireDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyTireDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTireDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTireDigestValidator(): LegacyTireDigestValidator {
    return LegacyTireDigestValidator(
        mutableListOf<LegacyTireDigestRule>(
            LegacyTireDigestDriverIdLengthRule(120),
            LegacyTireDigestRequiredCodeRule("legacy tire digest needs an entry"),
            LegacyTireDigestDescriptionLengthRule(80),
        ),
    )
}
