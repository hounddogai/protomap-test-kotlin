package com.acme.transport.emissions.idling.review

import com.acme.transport.kit.text.normalize

/** A rule a idling schedule must pass. Check returns its problems, or none. */
interface IdlingScheduleRule {
    fun check(item: IdlingSchedule): MutableList<String>
}

data class IdlingScheduleCodePrefixRule(var prefix: String) : IdlingScheduleRule {
    override fun check(item: IdlingSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IdlingScheduleRequiredDockRule(var message: String) : IdlingScheduleRule {
    override fun check(item: IdlingSchedule): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingScheduleDescriptionLengthRule(var maxLength: Int) : IdlingScheduleRule {
    override fun check(item: IdlingSchedule): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingScheduleNameLengthRule(var maxLength: Int) : IdlingScheduleRule {
    override fun check(item: IdlingSchedule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingScheduleRequiredNameRule(var message: String) : IdlingScheduleRule {
    override fun check(item: IdlingSchedule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingScheduleAllowedNameRule(var allowed: MutableList<String>) : IdlingScheduleRule {
    override fun check(item: IdlingSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling schedule. */
data class IdlingScheduleValidator(var rules: MutableList<IdlingScheduleRule>) {
    fun validate(item: IdlingSchedule): MutableList<String> {
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

    fun requireValid(item: IdlingSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingScheduleValidator(): IdlingScheduleValidator {
    return IdlingScheduleValidator(
        mutableListOf<IdlingScheduleRule>(
            IdlingScheduleCodePrefixRule("X"),
            IdlingScheduleRequiredDockRule("idling schedule needs an entry"),
            IdlingScheduleDescriptionLengthRule(64),
            IdlingScheduleNameLengthRule(80),
            IdlingScheduleRequiredNameRule("idling schedule needs this field"),
            IdlingScheduleAllowedNameRule(mutableListOf<String>("manual", "batched", "tiered")),
        ),
    )
}
