package com.acme.transport.freightpay.fuelsurcharge.strictsurchargesnapshot

import com.acme.transport.kit.text.normalize

/** A rule a strict fuel surcharge target must pass. Check returns its problems, or none. */
interface StrictFuelSurchargeTargetRule {
    fun check(item: StrictFuelSurchargeTarget): MutableList<String>
}

data class StrictFuelSurchargeTargetAllowedNameRule(
    var allowed: MutableList<String>,
) : StrictFuelSurchargeTargetRule {
    override fun check(item: StrictFuelSurchargeTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class StrictFuelSurchargeTargetCodeLengthRule(var maxLength: Int) : StrictFuelSurchargeTargetRule {
    override fun check(item: StrictFuelSurchargeTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictFuelSurchargeTargetStopCountRangeRule(
    var min: Int,
    var max: Int,
) : StrictFuelSurchargeTargetRule {
    override fun check(item: StrictFuelSurchargeTarget): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class StrictFuelSurchargeTargetRequiredNameRule(var message: String) : StrictFuelSurchargeTargetRule {
    override fun check(item: StrictFuelSurchargeTarget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictFuelSurchargeTargetRequiredCodeRule(var message: String) : StrictFuelSurchargeTargetRule {
    override fun check(item: StrictFuelSurchargeTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict fuel surcharge target. */
data class StrictFuelSurchargeTargetValidator(var rules: MutableList<StrictFuelSurchargeTargetRule>) {
    fun validate(item: StrictFuelSurchargeTarget): MutableList<String> {
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

    fun requireValid(item: StrictFuelSurchargeTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictFuelSurchargeTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictFuelSurchargeTargetValidator(): StrictFuelSurchargeTargetValidator {
    return StrictFuelSurchargeTargetValidator(
        mutableListOf<StrictFuelSurchargeTargetRule>(
            StrictFuelSurchargeTargetAllowedNameRule(mutableListOf<String>()),
            StrictFuelSurchargeTargetCodeLengthRule(86400),
            StrictFuelSurchargeTargetStopCountRangeRule(64, 120),
            StrictFuelSurchargeTargetRequiredNameRule("strict fuel surcharge target needs an entry"),
            StrictFuelSurchargeTargetRequiredCodeRule("strict fuel surcharge target needs this field"),
        ),
    )
}
