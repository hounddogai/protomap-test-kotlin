package com.acme.transport.customs.hold.milestone

import com.acme.transport.kit.text.normalize

/** A rule a hold entry must pass. Check returns its problems, or none. */
interface HoldEntryRule {
    fun check(item: HoldEntry): MutableList<String>
}

data class HoldEntryAllowedRegionRule(var allowed: MutableList<String>) : HoldEntryRule {
    override fun check(item: HoldEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class HoldEntryLabelLengthRule(var maxLength: Int) : HoldEntryRule {
    override fun check(item: HoldEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldEntryOwnerIdLengthRule(var maxLength: Int) : HoldEntryRule {
    override fun check(item: HoldEntry): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldEntryRequiredCountryRule(var message: String) : HoldEntryRule {
    override fun check(item: HoldEntry): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold entry. */
data class HoldEntryValidator(var rules: MutableList<HoldEntryRule>) {
    fun validate(item: HoldEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HoldEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldEntryValidator(): HoldEntryValidator {
    return HoldEntryValidator(
        mutableListOf<HoldEntryRule>(
            HoldEntryAllowedRegionRule(mutableListOf<String>()),
            HoldEntryLabelLengthRule(120),
            HoldEntryOwnerIdLengthRule(120),
            HoldEntryRequiredCountryRule("hold entry needs an entry"),
        ),
    )
}
