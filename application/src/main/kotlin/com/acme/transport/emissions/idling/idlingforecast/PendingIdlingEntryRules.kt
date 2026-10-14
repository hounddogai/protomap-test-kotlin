package com.acme.transport.emissions.idling.idlingforecast

import com.acme.transport.kit.text.normalize

/** A rule a pending idling entry must pass. Check returns its problems, or none. */
interface PendingIdlingEntryRule {
    fun check(item: PendingIdlingEntry): MutableList<String>
}

data class PendingIdlingEntryMinimumRangeRule(var min: Double, var max: Double) : PendingIdlingEntryRule {
    override fun check(item: PendingIdlingEntry): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingEntryAllowedCodeRule(var allowed: MutableList<String>) : PendingIdlingEntryRule {
    override fun check(item: PendingIdlingEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingEntryVersionRangeRule(var min: Int, var max: Int) : PendingIdlingEntryRule {
    override fun check(item: PendingIdlingEntry): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingEntryCodeLengthRule(var maxLength: Int) : PendingIdlingEntryRule {
    override fun check(item: PendingIdlingEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending idling entry. */
data class PendingIdlingEntryValidator(var rules: MutableList<PendingIdlingEntryRule>) {
    fun validate(item: PendingIdlingEntry): MutableList<String> {
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

    fun requireValid(item: PendingIdlingEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingIdlingEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingIdlingEntryValidator(): PendingIdlingEntryValidator {
    return PendingIdlingEntryValidator(
        mutableListOf<PendingIdlingEntryRule>(
            PendingIdlingEntryMinimumRangeRule(6.0, 3361.6),
            PendingIdlingEntryAllowedCodeRule(mutableListOf<String>()),
            PendingIdlingEntryVersionRangeRule(80, 1000),
            PendingIdlingEntryCodeLengthRule(255),
        ),
    )
}
