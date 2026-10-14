package com.acme.transport.network.zone.digest

/** A rule a zone digest must pass. Check returns its problems, or none. */
interface ZoneDigestRule {
    fun check(item: ZoneDigest): MutableList<String>
}

data class ZoneDigestTitleLengthRule(var maxLength: Int) : ZoneDigestRule {
    override fun check(item: ZoneDigest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneDigestEndsAtRangeRule(var min: Int, var max: Int) : ZoneDigestRule {
    override fun check(item: ZoneDigest): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneDigestApprovedGuardRule(var limit: Double) : ZoneDigestRule {
    override fun check(item: ZoneDigest): MutableList<String> {
        if (item.approved && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class ZoneDigestRequiredCodeRule(var message: String) : ZoneDigestRule {
    override fun check(item: ZoneDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneDigestMaximumRangeRule(var min: Double, var max: Double) : ZoneDigestRule {
    override fun check(item: ZoneDigest): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneDigestCountryLengthRule(var maxLength: Int) : ZoneDigestRule {
    override fun check(item: ZoneDigest): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone digest. */
data class ZoneDigestValidator(var rules: MutableList<ZoneDigestRule>) {
    fun validate(item: ZoneDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZoneDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneDigestValidator(): ZoneDigestValidator {
    return ZoneDigestValidator(
        mutableListOf<ZoneDigestRule>(
            ZoneDigestTitleLengthRule(86400),
            ZoneDigestEndsAtRangeRule(64, 255),
            ZoneDigestApprovedGuardRule(9.1),
            ZoneDigestRequiredCodeRule("zone digest needs a value"),
            ZoneDigestMaximumRangeRule(2.2, 887.2),
            ZoneDigestCountryLengthRule(255),
        ),
    )
}
