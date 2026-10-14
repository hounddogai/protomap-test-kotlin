package com.acme.transport.lastmile.redelivery.budget

import com.acme.transport.kit.text.normalize

/** A rule a global redelivery bundle must pass. Check returns its problems, or none. */
interface GlobalRedeliveryBundleRule {
    fun check(item: GlobalRedeliveryBundle): MutableList<String>
}

data class GlobalRedeliveryBundleRateRangeRule(var min: Double, var max: Double) : GlobalRedeliveryBundleRule {
    override fun check(item: GlobalRedeliveryBundle): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliveryBundleRequiredUnitRule(var message: String) : GlobalRedeliveryBundleRule {
    override fun check(item: GlobalRedeliveryBundle): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliveryBundleAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : GlobalRedeliveryBundleRule {
    override fun check(item: GlobalRedeliveryBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliveryBundleCodeLengthRule(var maxLength: Int) : GlobalRedeliveryBundleRule {
    override fun check(item: GlobalRedeliveryBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliveryBundleRequiredLabelRule(var message: String) : GlobalRedeliveryBundleRule {
    override fun check(item: GlobalRedeliveryBundle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global redelivery bundle. */
data class GlobalRedeliveryBundleValidator(var rules: MutableList<GlobalRedeliveryBundleRule>) {
    fun validate(item: GlobalRedeliveryBundle): MutableList<String> {
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

    fun requireValid(item: GlobalRedeliveryBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRedeliveryBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRedeliveryBundleValidator(): GlobalRedeliveryBundleValidator {
    return GlobalRedeliveryBundleValidator(
        mutableListOf<GlobalRedeliveryBundleRule>(
            GlobalRedeliveryBundleRateRangeRule(0.7, 4903.9),
            GlobalRedeliveryBundleRequiredUnitRule("global redelivery bundle needs a value"),
            GlobalRedeliveryBundleAllowedOwnerIdRule(mutableListOf<String>("global")),
            GlobalRedeliveryBundleCodeLengthRule(3600),
            GlobalRedeliveryBundleRequiredLabelRule("global redelivery bundle needs a value"),
        ),
    )
}
