package com.acme.transport.network.zone.digest

import com.acme.transport.kit.text.normalize

/** A rule a zone allocation must pass. Check returns its problems, or none. */
interface ZoneAllocationRule {
    fun check(item: ZoneAllocation): MutableList<String>
}

data class ZoneAllocationRequiredCodeRule(var message: String) : ZoneAllocationRule {
    override fun check(item: ZoneAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneAllocationRequiredVehicleIdRule(var message: String) : ZoneAllocationRule {
    override fun check(item: ZoneAllocation): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneAllocationCodePrefixRule(var prefix: String) : ZoneAllocationRule {
    override fun check(item: ZoneAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ZoneAllocationAllowedCodeRule(var allowed: MutableList<String>) : ZoneAllocationRule {
    override fun check(item: ZoneAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone allocation. */
data class ZoneAllocationValidator(var rules: MutableList<ZoneAllocationRule>) {
    fun validate(item: ZoneAllocation): MutableList<String> {
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

    fun requireValid(item: ZoneAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneAllocationValidator(): ZoneAllocationValidator {
    return ZoneAllocationValidator(
        mutableListOf<ZoneAllocationRule>(
            ZoneAllocationRequiredCodeRule("zone allocation needs an entry"),
            ZoneAllocationRequiredVehicleIdRule("zone allocation needs a value"),
            ZoneAllocationCodePrefixRule("OPS"),
            ZoneAllocationAllowedCodeRule(mutableListOf<String>("fallback", "weighted", "rolling")),
        ),
    )
}
