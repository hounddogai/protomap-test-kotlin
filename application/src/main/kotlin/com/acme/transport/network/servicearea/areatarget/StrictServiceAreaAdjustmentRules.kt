package com.acme.transport.network.servicearea.areatarget

import com.acme.transport.kit.text.normalize

/** A rule a strict service area adjustment must pass. Check returns its problems, or none. */
interface StrictServiceAreaAdjustmentRule {
    fun check(item: StrictServiceAreaAdjustment): MutableList<String>
}

data class StrictServiceAreaAdjustmentAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : StrictServiceAreaAdjustmentRule {
    override fun check(item: StrictServiceAreaAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaAdjustmentCodePrefixRule(var prefix: String) : StrictServiceAreaAdjustmentRule {
    override fun check(item: StrictServiceAreaAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaAdjustmentQuantityRangeRule(
    var min: Int,
    var max: Int,
) : StrictServiceAreaAdjustmentRule {
    override fun check(item: StrictServiceAreaAdjustment): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class StrictServiceAreaAdjustmentRequiredCodeRule(var message: String) : StrictServiceAreaAdjustmentRule {
    override fun check(item: StrictServiceAreaAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaAdjustmentCodeLengthRule(var maxLength: Int) : StrictServiceAreaAdjustmentRule {
    override fun check(item: StrictServiceAreaAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict service area adjustment. */
data class StrictServiceAreaAdjustmentValidator(var rules: MutableList<StrictServiceAreaAdjustmentRule>) {
    fun validate(item: StrictServiceAreaAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictServiceAreaAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictServiceAreaAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictServiceAreaAdjustmentValidator(): StrictServiceAreaAdjustmentValidator {
    return StrictServiceAreaAdjustmentValidator(
        mutableListOf<StrictServiceAreaAdjustmentRule>(
            StrictServiceAreaAdjustmentAllowedOwnerIdRule(
                mutableListOf<String>("global", "capped", "scheduled"),
            ),
            StrictServiceAreaAdjustmentCodePrefixRule("OPS"),
            StrictServiceAreaAdjustmentQuantityRangeRule(32, 120),
            StrictServiceAreaAdjustmentRequiredCodeRule("strict service area adjustment needs this field"),
            StrictServiceAreaAdjustmentCodeLengthRule(64),
        ),
    )
}
