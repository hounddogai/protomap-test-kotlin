package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.kit.text.normalize

/** A rule a lenient idling entry must pass. Check returns its problems, or none. */
interface LenientIdlingEntryRule {
    fun check(item: LenientIdlingEntry): MutableList<String>
}

data class LenientIdlingEntryCodeLengthRule(var maxLength: Int) : LenientIdlingEntryRule {
    override fun check(item: LenientIdlingEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientIdlingEntryRequiredCodeRule(var message: String) : LenientIdlingEntryRule {
    override fun check(item: LenientIdlingEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientIdlingEntryAllowedVehicleIdRule(var allowed: MutableList<String>) : LenientIdlingEntryRule {
    override fun check(item: LenientIdlingEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class LenientIdlingEntryRequiredTitleRule(var message: String) : LenientIdlingEntryRule {
    override fun check(item: LenientIdlingEntry): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientIdlingEntryTitleLengthRule(var maxLength: Int) : LenientIdlingEntryRule {
    override fun check(item: LenientIdlingEntry): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientIdlingEntryCodePrefixRule(var prefix: String) : LenientIdlingEntryRule {
    override fun check(item: LenientIdlingEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient idling entry. */
data class LenientIdlingEntryValidator(var rules: MutableList<LenientIdlingEntryRule>) {
    fun validate(item: LenientIdlingEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientIdlingEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientIdlingEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientIdlingEntryValidator(): LenientIdlingEntryValidator {
    return LenientIdlingEntryValidator(
        mutableListOf<LenientIdlingEntryRule>(
            LenientIdlingEntryCodeLengthRule(32),
            LenientIdlingEntryRequiredCodeRule("lenient idling entry needs a value"),
            LenientIdlingEntryAllowedVehicleIdRule(mutableListOf<String>("regional")),
            LenientIdlingEntryRequiredTitleRule("lenient idling entry needs a value"),
            LenientIdlingEntryTitleLengthRule(255),
            LenientIdlingEntryCodePrefixRule("OPS"),
        ),
    )
}
