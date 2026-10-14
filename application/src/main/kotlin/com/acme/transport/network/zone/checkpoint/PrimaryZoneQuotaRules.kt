package com.acme.transport.network.zone.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a primary zone quota must pass. Check returns its problems, or none. */
interface PrimaryZoneQuotaRule {
    fun check(item: PrimaryZoneQuota): MutableList<String>
}

data class PrimaryZoneQuotaAllowedDriverIdRule(var allowed: MutableList<String>) : PrimaryZoneQuotaRule {
    override fun check(item: PrimaryZoneQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryZoneQuotaRequiredTitleRule(var message: String) : PrimaryZoneQuotaRule {
    override fun check(item: PrimaryZoneQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryZoneQuotaTitleLengthRule(var maxLength: Int) : PrimaryZoneQuotaRule {
    override fun check(item: PrimaryZoneQuota): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryZoneQuotaCreatedAtRangeRule(var min: Int, var max: Int) : PrimaryZoneQuotaRule {
    override fun check(item: PrimaryZoneQuota): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class PrimaryZoneQuotaRequiredNameRule(var message: String) : PrimaryZoneQuotaRule {
    override fun check(item: PrimaryZoneQuota): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary zone quota. */
data class PrimaryZoneQuotaValidator(var rules: MutableList<PrimaryZoneQuotaRule>) {
    fun validate(item: PrimaryZoneQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryZoneQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryZoneQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryZoneQuotaValidator(): PrimaryZoneQuotaValidator {
    return PrimaryZoneQuotaValidator(
        mutableListOf<PrimaryZoneQuotaRule>(
            PrimaryZoneQuotaAllowedDriverIdRule(mutableListOf<String>("strict")),
            PrimaryZoneQuotaRequiredTitleRule("primary zone quota needs a value"),
            PrimaryZoneQuotaTitleLengthRule(1000),
            PrimaryZoneQuotaCreatedAtRangeRule(120, 255),
            PrimaryZoneQuotaRequiredNameRule("primary zone quota needs this field"),
        ),
    )
}
