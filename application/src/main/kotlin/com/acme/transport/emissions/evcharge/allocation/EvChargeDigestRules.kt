package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.kit.text.normalize

/** A rule a ev charge digest must pass. Check returns its problems, or none. */
interface EvChargeDigestRule {
    fun check(item: EvChargeDigest): MutableList<String>
}

data class EvChargeDigestVehicleIdLengthRule(var maxLength: Int) : EvChargeDigestRule {
    override fun check(item: EvChargeDigest): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeDigestRequiredCodeRule(var message: String) : EvChargeDigestRule {
    override fun check(item: EvChargeDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeDigestOdometerRangeRule(var min: Int, var max: Int) : EvChargeDigestRule {
    override fun check(item: EvChargeDigest): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeDigestAllowedCodeRule(var allowed: MutableList<String>) : EvChargeDigestRule {
    override fun check(item: EvChargeDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge digest. */
data class EvChargeDigestValidator(var rules: MutableList<EvChargeDigestRule>) {
    fun validate(item: EvChargeDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeDigestValidator(): EvChargeDigestValidator {
    return EvChargeDigestValidator(
        mutableListOf<EvChargeDigestRule>(
            EvChargeDigestVehicleIdLengthRule(1000),
            EvChargeDigestRequiredCodeRule("ev charge digest needs this field"),
            EvChargeDigestOdometerRangeRule(32, 255),
            EvChargeDigestAllowedCodeRule(mutableListOf<String>("seasonal", "batched", "rolling")),
        ),
    )
}
