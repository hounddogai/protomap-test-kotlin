package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.text.normalize

/** A rule a incident request must pass. Check returns its problems, or none. */
interface IncidentRequestRule {
    fun check(item: IncidentRequest): MutableList<String>
}

data class IncidentRequestRequiredCodeRule(var message: String) : IncidentRequestRule {
    override fun check(item: IncidentRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentRequestRequiredTitleRule(var message: String) : IncidentRequestRule {
    override fun check(item: IncidentRequest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentRequestWindowDaysRangeRule(var min: Int, var max: Int) : IncidentRequestRule {
    override fun check(item: IncidentRequest): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class IncidentRequestAllowedCodeRule(var allowed: MutableList<String>) : IncidentRequestRule {
    override fun check(item: IncidentRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class IncidentRequestRequiredPhoneRule(var message: String) : IncidentRequestRule {
    override fun check(item: IncidentRequest): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentRequestCodePrefixRule(var prefix: String) : IncidentRequestRule {
    override fun check(item: IncidentRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident request. */
data class IncidentRequestValidator(var rules: MutableList<IncidentRequestRule>) {
    fun validate(item: IncidentRequest): MutableList<String> {
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

    fun requireValid(item: IncidentRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentRequestValidator(): IncidentRequestValidator {
    return IncidentRequestValidator(
        mutableListOf<IncidentRequestRule>(
            IncidentRequestRequiredCodeRule("incident request needs a value"),
            IncidentRequestRequiredTitleRule("incident request needs a value"),
            IncidentRequestWindowDaysRangeRule(64, 1000),
            IncidentRequestAllowedCodeRule(mutableListOf<String>("legacy", "legacy")),
            IncidentRequestRequiredPhoneRule("incident request needs an entry"),
            IncidentRequestCodePrefixRule("ACM"),
        ),
    )
}
