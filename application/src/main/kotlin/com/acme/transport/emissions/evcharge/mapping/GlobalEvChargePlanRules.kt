package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.kit.text.normalize

/** A rule a global ev charge plan must pass. Check returns its problems, or none. */
interface GlobalEvChargePlanRule {
    fun check(item: GlobalEvChargePlan): MutableList<String>
}

data class GlobalEvChargePlanDockLengthRule(var maxLength: Int) : GlobalEvChargePlanRule {
    override fun check(item: GlobalEvChargePlan): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalEvChargePlanRequiredCodeRule(var message: String) : GlobalEvChargePlanRule {
    override fun check(item: GlobalEvChargePlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalEvChargePlanAllowedNameRule(var allowed: MutableList<String>) : GlobalEvChargePlanRule {
    override fun check(item: GlobalEvChargePlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

data class GlobalEvChargePlanRequiredDockRule(var message: String) : GlobalEvChargePlanRule {
    override fun check(item: GlobalEvChargePlan): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalEvChargePlanCodePrefixRule(var prefix: String) : GlobalEvChargePlanRule {
    override fun check(item: GlobalEvChargePlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalEvChargePlanRequiredCarrierRule(var message: String) : GlobalEvChargePlanRule {
    override fun check(item: GlobalEvChargePlan): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global ev charge plan. */
data class GlobalEvChargePlanValidator(var rules: MutableList<GlobalEvChargePlanRule>) {
    fun validate(item: GlobalEvChargePlan): MutableList<String> {
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

    fun requireValid(item: GlobalEvChargePlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalEvChargePlan>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalEvChargePlanValidator(): GlobalEvChargePlanValidator {
    return GlobalEvChargePlanValidator(
        mutableListOf<GlobalEvChargePlanRule>(
            GlobalEvChargePlanDockLengthRule(80),
            GlobalEvChargePlanRequiredCodeRule("global ev charge plan needs a value"),
            GlobalEvChargePlanAllowedNameRule(mutableListOf<String>("adaptive", "tiered")),
            GlobalEvChargePlanRequiredDockRule("global ev charge plan needs an entry"),
            GlobalEvChargePlanCodePrefixRule("Q"),
            GlobalEvChargePlanRequiredCarrierRule("global ev charge plan needs an entry"),
        ),
    )
}
