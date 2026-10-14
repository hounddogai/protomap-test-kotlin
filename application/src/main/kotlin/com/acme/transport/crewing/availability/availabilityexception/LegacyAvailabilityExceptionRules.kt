package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.kit.text.normalize

/** A rule a legacy availability exception must pass. Check returns its problems, or none. */
interface LegacyAvailabilityExceptionRule {
    fun check(item: LegacyAvailabilityException): MutableList<String>
}

data class LegacyAvailabilityExceptionCodePrefixRule(var prefix: String) : LegacyAvailabilityExceptionRule {
    override fun check(item: LegacyAvailabilityException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAvailabilityExceptionCodeLengthRule(var maxLength: Int) : LegacyAvailabilityExceptionRule {
    override fun check(item: LegacyAvailabilityException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAvailabilityExceptionRequiredNameRule(var message: String) : LegacyAvailabilityExceptionRule {
    override fun check(item: LegacyAvailabilityException): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyAvailabilityExceptionRequiredVehicleIdRule(
    var message: String,
) : LegacyAvailabilityExceptionRule {
    override fun check(item: LegacyAvailabilityException): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyAvailabilityExceptionAllowedCodeRule(
    var allowed: MutableList<String>,
) : LegacyAvailabilityExceptionRule {
    override fun check(item: LegacyAvailabilityException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy availability exception. */
data class LegacyAvailabilityExceptionValidator(var rules: MutableList<LegacyAvailabilityExceptionRule>) {
    fun validate(item: LegacyAvailabilityException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyAvailabilityException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyAvailabilityException>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyAvailabilityExceptionValidator(): LegacyAvailabilityExceptionValidator {
    return LegacyAvailabilityExceptionValidator(
        mutableListOf<LegacyAvailabilityExceptionRule>(
            LegacyAvailabilityExceptionCodePrefixRule("ACM"),
            LegacyAvailabilityExceptionCodeLengthRule(120),
            LegacyAvailabilityExceptionRequiredNameRule("legacy availability exception needs a value"),
            LegacyAvailabilityExceptionRequiredVehicleIdRule("legacy availability exception needs this field"),
            LegacyAvailabilityExceptionAllowedCodeRule(mutableListOf<String>("lenient", "fallback")),
        ),
    )
}
