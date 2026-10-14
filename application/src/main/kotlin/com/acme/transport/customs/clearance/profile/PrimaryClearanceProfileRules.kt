package com.acme.transport.customs.clearance.profile

import com.acme.transport.kit.text.normalize

/** A rule a primary clearance profile must pass. Check returns its problems, or none. */
interface PrimaryClearanceProfileRule {
    fun check(item: PrimaryClearanceProfile): MutableList<String>
}

data class PrimaryClearanceProfileCodePrefixRule(var prefix: String) : PrimaryClearanceProfileRule {
    override fun check(item: PrimaryClearanceProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceProfileRequiredExternalRefRule(var message: String) : PrimaryClearanceProfileRule {
    override fun check(item: PrimaryClearanceProfile): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceProfileDescriptionLengthRule(var maxLength: Int) : PrimaryClearanceProfileRule {
    override fun check(item: PrimaryClearanceProfile): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceProfileFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryClearanceProfileRule {
    override fun check(item: PrimaryClearanceProfile): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceProfileAllowedCodeRule(
    var allowed: MutableList<String>,
) : PrimaryClearanceProfileRule {
    override fun check(item: PrimaryClearanceProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary clearance profile. */
data class PrimaryClearanceProfileValidator(var rules: MutableList<PrimaryClearanceProfileRule>) {
    fun validate(item: PrimaryClearanceProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryClearanceProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryClearanceProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryClearanceProfileValidator(): PrimaryClearanceProfileValidator {
    return PrimaryClearanceProfileValidator(
        mutableListOf<PrimaryClearanceProfileRule>(
            PrimaryClearanceProfileCodePrefixRule("OPS"),
            PrimaryClearanceProfileRequiredExternalRefRule("primary clearance profile needs a value"),
            PrimaryClearanceProfileDescriptionLengthRule(64),
            PrimaryClearanceProfileFuelLitersRangeRule(3.8, 4310.7),
            PrimaryClearanceProfileAllowedCodeRule(mutableListOf<String>("scheduled", "default")),
        ),
    )
}
