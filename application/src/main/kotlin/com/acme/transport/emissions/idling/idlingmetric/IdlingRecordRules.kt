package com.acme.transport.emissions.idling.idlingmetric

import com.acme.transport.kit.text.normalize

/** A rule a idling record must pass. Check returns its problems, or none. */
interface IdlingRecordRule {
    fun check(item: IdlingRecord): MutableList<String>
}

data class IdlingRecordRequiredRouteCodeRule(var message: String) : IdlingRecordRule {
    override fun check(item: IdlingRecord): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingRecordCodeLengthRule(var maxLength: Int) : IdlingRecordRule {
    override fun check(item: IdlingRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingRecordAllowedDescriptionRule(var allowed: MutableList<String>) : IdlingRecordRule {
    override fun check(item: IdlingRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class IdlingRecordCodePrefixRule(var prefix: String) : IdlingRecordRule {
    override fun check(item: IdlingRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling record. */
data class IdlingRecordValidator(var rules: MutableList<IdlingRecordRule>) {
    fun validate(item: IdlingRecord): MutableList<String> {
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

    fun requireValid(item: IdlingRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingRecordValidator(): IdlingRecordValidator {
    return IdlingRecordValidator(
        mutableListOf<IdlingRecordRule>(
            IdlingRecordRequiredRouteCodeRule("idling record needs an entry"),
            IdlingRecordCodeLengthRule(120),
            IdlingRecordAllowedDescriptionRule(mutableListOf<String>()),
            IdlingRecordCodePrefixRule("ACM"),
        ),
    )
}
