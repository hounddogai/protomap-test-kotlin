package com.acme.transport.safety.incident.incidentplan

import com.acme.transport.kit.text.normalize

/** A rule a regional incident exception must pass. Check returns its problems, or none. */
interface RegionalIncidentExceptionRule {
    fun check(item: RegionalIncidentException): MutableList<String>
}

data class RegionalIncidentExceptionNameLengthRule(var maxLength: Int) : RegionalIncidentExceptionRule {
    override fun check(item: RegionalIncidentException): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentExceptionTrailerIdLengthRule(var maxLength: Int) : RegionalIncidentExceptionRule {
    override fun check(item: RegionalIncidentException): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentExceptionCurrencyLengthRule(var maxLength: Int) : RegionalIncidentExceptionRule {
    override fun check(item: RegionalIncidentException): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentExceptionAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : RegionalIncidentExceptionRule {
    override fun check(item: RegionalIncidentException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentExceptionCodePrefixRule(var prefix: String) : RegionalIncidentExceptionRule {
    override fun check(item: RegionalIncidentException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentExceptionRequiredCurrencyRule(var message: String) : RegionalIncidentExceptionRule {
    override fun check(item: RegionalIncidentException): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional incident exception. */
data class RegionalIncidentExceptionValidator(var rules: MutableList<RegionalIncidentExceptionRule>) {
    fun validate(item: RegionalIncidentException): MutableList<String> {
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

    fun requireValid(item: RegionalIncidentException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalIncidentException>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalIncidentExceptionValidator(): RegionalIncidentExceptionValidator {
    return RegionalIncidentExceptionValidator(
        mutableListOf<RegionalIncidentExceptionRule>(
            RegionalIncidentExceptionNameLengthRule(3600),
            RegionalIncidentExceptionTrailerIdLengthRule(64),
            RegionalIncidentExceptionCurrencyLengthRule(32),
            RegionalIncidentExceptionAllowedTrailerIdRule(
                mutableListOf<String>("weighted", "adaptive", "primary"),
            ),
            RegionalIncidentExceptionCodePrefixRule("Q"),
            RegionalIncidentExceptionRequiredCurrencyRule("regional incident exception needs this field"),
        ),
    )
}
