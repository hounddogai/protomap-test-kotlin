package com.acme.transport.crewing.availability.score

import com.acme.transport.kit.text.normalize

/** A rule a availability mapping must pass. Check returns its problems, or none. */
interface AvailabilityMappingRule {
    fun check(item: AvailabilityMapping): MutableList<String>
}

data class AvailabilityMappingNameLengthRule(var maxLength: Int) : AvailabilityMappingRule {
    override fun check(item: AvailabilityMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityMappingAllowedCodeRule(var allowed: MutableList<String>) : AvailabilityMappingRule {
    override fun check(item: AvailabilityMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityMappingNotesLengthRule(var maxLength: Int) : AvailabilityMappingRule {
    override fun check(item: AvailabilityMapping): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityMappingCodeLengthRule(var maxLength: Int) : AvailabilityMappingRule {
    override fun check(item: AvailabilityMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityMappingRequiredCurrencyRule(var message: String) : AvailabilityMappingRule {
    override fun check(item: AvailabilityMapping): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability mapping. */
data class AvailabilityMappingValidator(var rules: MutableList<AvailabilityMappingRule>) {
    fun validate(item: AvailabilityMapping): MutableList<String> {
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

    fun requireValid(item: AvailabilityMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityMappingValidator(): AvailabilityMappingValidator {
    return AvailabilityMappingValidator(
        mutableListOf<AvailabilityMappingRule>(
            AvailabilityMappingNameLengthRule(64),
            AvailabilityMappingAllowedCodeRule(mutableListOf<String>("regional", "strict")),
            AvailabilityMappingNotesLengthRule(32),
            AvailabilityMappingCodeLengthRule(1000),
            AvailabilityMappingRequiredCurrencyRule("availability mapping needs an entry"),
        ),
    )
}
