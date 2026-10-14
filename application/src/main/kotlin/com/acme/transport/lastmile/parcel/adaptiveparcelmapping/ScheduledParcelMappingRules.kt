package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.kit.text.normalize

/** A rule a scheduled parcel mapping must pass. Check returns its problems, or none. */
interface ScheduledParcelMappingRule {
    fun check(item: ScheduledParcelMapping): MutableList<String>
}

data class ScheduledParcelMappingAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledParcelMappingRule {
    override fun check(item: ScheduledParcelMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledParcelMappingNotesLengthRule(var maxLength: Int) : ScheduledParcelMappingRule {
    override fun check(item: ScheduledParcelMapping): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledParcelMappingRequiredTitleRule(var message: String) : ScheduledParcelMappingRule {
    override fun check(item: ScheduledParcelMapping): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled parcel mapping. */
data class ScheduledParcelMappingValidator(var rules: MutableList<ScheduledParcelMappingRule>) {
    fun validate(item: ScheduledParcelMapping): MutableList<String> {
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

    fun requireValid(item: ScheduledParcelMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledParcelMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledParcelMappingValidator(): ScheduledParcelMappingValidator {
    return ScheduledParcelMappingValidator(
        mutableListOf<ScheduledParcelMappingRule>(
            ScheduledParcelMappingAllowedCodeRule(mutableListOf<String>("fallback", "rolling", "adaptive")),
            ScheduledParcelMappingNotesLengthRule(1000),
            ScheduledParcelMappingRequiredTitleRule("scheduled parcel mapping needs an entry"),
        ),
    )
}
