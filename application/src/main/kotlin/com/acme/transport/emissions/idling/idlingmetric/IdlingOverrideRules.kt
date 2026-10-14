package com.acme.transport.emissions.idling.idlingmetric

import com.acme.transport.kit.text.normalize

/** A rule a idling override must pass. Check returns its problems, or none. */
interface IdlingOverrideRule {
    fun check(item: IdlingOverride): MutableList<String>
}

data class IdlingOverrideRequiredCodeRule(var message: String) : IdlingOverrideRule {
    override fun check(item: IdlingOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingOverrideRequiredTitleRule(var message: String) : IdlingOverrideRule {
    override fun check(item: IdlingOverride): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingOverrideAllowedCarrierRule(var allowed: MutableList<String>) : IdlingOverrideRule {
    override fun check(item: IdlingOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class IdlingOverrideFloorValueRangeRule(var min: Double, var max: Double) : IdlingOverrideRule {
    override fun check(item: IdlingOverride): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class IdlingOverrideDiscountRangeRule(var min: Double, var max: Double) : IdlingOverrideRule {
    override fun check(item: IdlingOverride): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling override. */
data class IdlingOverrideValidator(var rules: MutableList<IdlingOverrideRule>) {
    fun validate(item: IdlingOverride): MutableList<String> {
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

    fun requireValid(item: IdlingOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingOverrideValidator(): IdlingOverrideValidator {
    return IdlingOverrideValidator(
        mutableListOf<IdlingOverrideRule>(
            IdlingOverrideRequiredCodeRule("idling override needs a value"),
            IdlingOverrideRequiredTitleRule("idling override needs an entry"),
            IdlingOverrideAllowedCarrierRule(mutableListOf<String>("rolling", "tiered", "pending")),
            IdlingOverrideFloorValueRangeRule(5.4, 1556.2),
            IdlingOverrideDiscountRangeRule(2.5, 3974.2),
        ),
    )
}
