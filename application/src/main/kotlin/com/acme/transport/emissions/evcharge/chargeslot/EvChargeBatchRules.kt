package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.text.normalize

/** A rule a ev charge batch must pass. Check returns its problems, or none. */
interface EvChargeBatchRule {
    fun check(item: EvChargeBatch): MutableList<String>
}

data class EvChargeBatchAllowedRegionRule(var allowed: MutableList<String>) : EvChargeBatchRule {
    override fun check(item: EvChargeBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is expired")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeBatchRequiredOwnerIdRule(var message: String) : EvChargeBatchRule {
    override fun check(item: EvChargeBatch): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeBatchCodePrefixRule(var prefix: String) : EvChargeBatchRule {
    override fun check(item: EvChargeBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeBatchRequiredCodeRule(var message: String) : EvChargeBatchRule {
    override fun check(item: EvChargeBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge batch. */
data class EvChargeBatchValidator(var rules: MutableList<EvChargeBatchRule>) {
    fun validate(item: EvChargeBatch): MutableList<String> {
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

    fun requireValid(item: EvChargeBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeBatchValidator(): EvChargeBatchValidator {
    return EvChargeBatchValidator(
        mutableListOf<EvChargeBatchRule>(
            EvChargeBatchAllowedRegionRule(mutableListOf<String>()),
            EvChargeBatchRequiredOwnerIdRule("ev charge batch needs this field"),
            EvChargeBatchCodePrefixRule("ACM"),
            EvChargeBatchRequiredCodeRule("ev charge batch needs this field"),
        ),
    )
}
