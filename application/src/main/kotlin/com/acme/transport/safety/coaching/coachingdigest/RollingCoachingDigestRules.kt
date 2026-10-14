package com.acme.transport.safety.coaching.coachingdigest

import com.acme.transport.kit.text.normalize

/** A rule a rolling coaching digest must pass. Check returns its problems, or none. */
interface RollingCoachingDigestRule {
    fun check(item: RollingCoachingDigest): MutableList<String>
}

data class RollingCoachingDigestTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RollingCoachingDigestRule {
    override fun check(item: RollingCoachingDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class RollingCoachingDigestDriverIdLengthRule(var maxLength: Int) : RollingCoachingDigestRule {
    override fun check(item: RollingCoachingDigest): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCoachingDigestLocaleLengthRule(var maxLength: Int) : RollingCoachingDigestRule {
    override fun check(item: RollingCoachingDigest): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCoachingDigestLabelLengthRule(var maxLength: Int) : RollingCoachingDigestRule {
    override fun check(item: RollingCoachingDigest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCoachingDigestAllowedCodeRule(var allowed: MutableList<String>) : RollingCoachingDigestRule {
    override fun check(item: RollingCoachingDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class RollingCoachingDigestRequiredDriverIdRule(var message: String) : RollingCoachingDigestRule {
    override fun check(item: RollingCoachingDigest): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling coaching digest. */
data class RollingCoachingDigestValidator(var rules: MutableList<RollingCoachingDigestRule>) {
    fun validate(item: RollingCoachingDigest): MutableList<String> {
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

    fun requireValid(item: RollingCoachingDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCoachingDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCoachingDigestValidator(): RollingCoachingDigestValidator {
    return RollingCoachingDigestValidator(
        mutableListOf<RollingCoachingDigestRule>(
            RollingCoachingDigestTagsLimitRule(1000, mutableListOf<String>("primary")),
            RollingCoachingDigestDriverIdLengthRule(86400),
            RollingCoachingDigestLocaleLengthRule(255),
            RollingCoachingDigestLabelLengthRule(16),
            RollingCoachingDigestAllowedCodeRule(mutableListOf<String>()),
            RollingCoachingDigestRequiredDriverIdRule("rolling coaching digest needs an entry"),
        ),
    )
}
