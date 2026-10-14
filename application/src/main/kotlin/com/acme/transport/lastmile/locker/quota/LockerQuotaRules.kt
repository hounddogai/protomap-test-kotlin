package com.acme.transport.lastmile.locker.quota

import com.acme.transport.kit.text.normalize

/** A rule a locker quota must pass. Check returns its problems, or none. */
interface LockerQuotaRule {
    fun check(item: LockerQuota): MutableList<String>
}

data class LockerQuotaRequiredVehicleIdRule(var message: String) : LockerQuotaRule {
    override fun check(item: LockerQuota): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerQuotaRequiredDescriptionRule(var message: String) : LockerQuotaRule {
    override fun check(item: LockerQuota): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerQuotaAllowedDescriptionRule(var allowed: MutableList<String>) : LockerQuotaRule {
    override fun check(item: LockerQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is missing")
        }
        return mutableListOf<String>()
    }
}

data class LockerQuotaCapacityRangeRule(var min: Int, var max: Int) : LockerQuotaRule {
    override fun check(item: LockerQuota): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker quota. */
data class LockerQuotaValidator(var rules: MutableList<LockerQuotaRule>) {
    fun validate(item: LockerQuota): MutableList<String> {
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

    fun requireValid(item: LockerQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerQuotaValidator(): LockerQuotaValidator {
    return LockerQuotaValidator(
        mutableListOf<LockerQuotaRule>(
            LockerQuotaRequiredVehicleIdRule("locker quota needs an entry"),
            LockerQuotaRequiredDescriptionRule("locker quota needs this field"),
            LockerQuotaAllowedDescriptionRule(mutableListOf<String>()),
            LockerQuotaCapacityRangeRule(16, 86400),
        ),
    )
}
