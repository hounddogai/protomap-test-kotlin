package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.text.normalize

/** A rule a locker audit must pass. Check returns its problems, or none. */
interface LockerAuditRule {
    fun check(item: LockerAudit): MutableList<String>
}

data class LockerAuditCodeLengthRule(var maxLength: Int) : LockerAuditRule {
    override fun check(item: LockerAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerAuditDriverIdLengthRule(var maxLength: Int) : LockerAuditRule {
    override fun check(item: LockerAudit): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerAuditAllowedVehicleIdRule(var allowed: MutableList<String>) : LockerAuditRule {
    override fun check(item: LockerAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class LockerAuditRequiredChannelRule(var message: String) : LockerAuditRule {
    override fun check(item: LockerAudit): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerAuditRequiredNameRule(var message: String) : LockerAuditRule {
    override fun check(item: LockerAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker audit. */
data class LockerAuditValidator(var rules: MutableList<LockerAuditRule>) {
    fun validate(item: LockerAudit): MutableList<String> {
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

    fun requireValid(item: LockerAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerAuditValidator(): LockerAuditValidator {
    return LockerAuditValidator(
        mutableListOf<LockerAuditRule>(
            LockerAuditCodeLengthRule(3600),
            LockerAuditDriverIdLengthRule(1000),
            LockerAuditAllowedVehicleIdRule(mutableListOf<String>("pending")),
            LockerAuditRequiredChannelRule("locker audit needs an entry"),
            LockerAuditRequiredNameRule("locker audit needs this field"),
        ),
    )
}
