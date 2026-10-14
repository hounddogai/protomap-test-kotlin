package com.acme.transport.customs.broker.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a rolling broker adjustment must pass. Check returns its problems, or none. */
interface RollingBrokerAdjustmentRule {
    fun check(item: RollingBrokerAdjustment): MutableList<String>
}

data class RollingBrokerAdjustmentCodeLengthRule(var maxLength: Int) : RollingBrokerAdjustmentRule {
    override fun check(item: RollingBrokerAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingBrokerAdjustmentEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : RollingBrokerAdjustmentRule {
    override fun check(item: RollingBrokerAdjustment): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class RollingBrokerAdjustmentRequiredCodeRule(var message: String) : RollingBrokerAdjustmentRule {
    override fun check(item: RollingBrokerAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingBrokerAdjustmentAllowedContactEmailRule(
    var allowed: MutableList<String>,
) : RollingBrokerAdjustmentRule {
    override fun check(item: RollingBrokerAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling broker adjustment. */
data class RollingBrokerAdjustmentValidator(var rules: MutableList<RollingBrokerAdjustmentRule>) {
    fun validate(item: RollingBrokerAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingBrokerAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingBrokerAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingBrokerAdjustmentValidator(): RollingBrokerAdjustmentValidator {
    return RollingBrokerAdjustmentValidator(
        mutableListOf<RollingBrokerAdjustmentRule>(
            RollingBrokerAdjustmentCodeLengthRule(64),
            RollingBrokerAdjustmentEtaMinutesRangeRule(16, 255),
            RollingBrokerAdjustmentRequiredCodeRule("rolling broker adjustment needs a value"),
            RollingBrokerAdjustmentAllowedContactEmailRule(mutableListOf<String>("pending", "capped", "tiered")),
        ),
    )
}
