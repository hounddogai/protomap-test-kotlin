package com.acme.transport.customs.inspection.batch

import com.acme.transport.kit.text.normalize

/** A rule a inspection record must pass. Check returns its problems, or none. */
interface InspectionRecordRule {
    fun check(item: InspectionRecord): MutableList<String>
}

data class InspectionRecordCodePrefixRule(var prefix: String) : InspectionRecordRule {
    override fun check(item: InspectionRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionRecordAllowedTitleRule(var allowed: MutableList<String>) : InspectionRecordRule {
    override fun check(item: InspectionRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class InspectionRecordRequiredTimezoneRule(var message: String) : InspectionRecordRule {
    override fun check(item: InspectionRecord): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionRecordRequiredCodeRule(var message: String) : InspectionRecordRule {
    override fun check(item: InspectionRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionRecordCodeLengthRule(var maxLength: Int) : InspectionRecordRule {
    override fun check(item: InspectionRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionRecordRequiredTitleRule(var message: String) : InspectionRecordRule {
    override fun check(item: InspectionRecord): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection record. */
data class InspectionRecordValidator(var rules: MutableList<InspectionRecordRule>) {
    fun validate(item: InspectionRecord): MutableList<String> {
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

    fun requireValid(item: InspectionRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionRecordValidator(): InspectionRecordValidator {
    return InspectionRecordValidator(
        mutableListOf<InspectionRecordRule>(
            InspectionRecordCodePrefixRule("REF"),
            InspectionRecordAllowedTitleRule(mutableListOf<String>()),
            InspectionRecordRequiredTimezoneRule("inspection record needs a value"),
            InspectionRecordRequiredCodeRule("inspection record needs a value"),
            InspectionRecordCodeLengthRule(86400),
            InspectionRecordRequiredTitleRule("inspection record needs a value"),
        ),
    )
}
