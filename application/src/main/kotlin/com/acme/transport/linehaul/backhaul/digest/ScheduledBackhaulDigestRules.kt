package com.acme.transport.linehaul.backhaul.digest

import com.acme.transport.kit.text.normalize

/** A rule a scheduled backhaul digest must pass. Check returns its problems, or none. */
interface ScheduledBackhaulDigestRule {
    fun check(item: ScheduledBackhaulDigest): MutableList<String>
}

data class ScheduledBackhaulDigestLocaleLengthRule(var maxLength: Int) : ScheduledBackhaulDigestRule {
    override fun check(item: ScheduledBackhaulDigest): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBackhaulDigestVehicleIdLengthRule(var maxLength: Int) : ScheduledBackhaulDigestRule {
    override fun check(item: ScheduledBackhaulDigest): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBackhaulDigestCodeLengthRule(var maxLength: Int) : ScheduledBackhaulDigestRule {
    override fun check(item: ScheduledBackhaulDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBackhaulDigestCodePrefixRule(var prefix: String) : ScheduledBackhaulDigestRule {
    override fun check(item: ScheduledBackhaulDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBackhaulDigestAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : ScheduledBackhaulDigestRule {
    override fun check(item: ScheduledBackhaulDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBackhaulDigestRequiredLocaleRule(var message: String) : ScheduledBackhaulDigestRule {
    override fun check(item: ScheduledBackhaulDigest): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled backhaul digest. */
data class ScheduledBackhaulDigestValidator(var rules: MutableList<ScheduledBackhaulDigestRule>) {
    fun validate(item: ScheduledBackhaulDigest): MutableList<String> {
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

    fun requireValid(item: ScheduledBackhaulDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledBackhaulDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledBackhaulDigestValidator(): ScheduledBackhaulDigestValidator {
    return ScheduledBackhaulDigestValidator(
        mutableListOf<ScheduledBackhaulDigestRule>(
            ScheduledBackhaulDigestLocaleLengthRule(3600),
            ScheduledBackhaulDigestVehicleIdLengthRule(32),
            ScheduledBackhaulDigestCodeLengthRule(3600),
            ScheduledBackhaulDigestCodePrefixRule("ACM"),
            ScheduledBackhaulDigestAllowedVehicleIdRule(mutableListOf<String>("pending", "pending")),
            ScheduledBackhaulDigestRequiredLocaleRule("scheduled backhaul digest needs an entry"),
        ),
    )
}
