package com.acme.transport.lastmile.parcel.schedule

import com.acme.transport.kit.text.normalize

/** A rule a regional parcel draft must pass. Check returns its problems, or none. */
interface RegionalParcelDraftRule {
    fun check(item: RegionalParcelDraft): MutableList<String>
}

data class RegionalParcelDraftAllowedOwnerIdRule(var allowed: MutableList<String>) : RegionalParcelDraftRule {
    override fun check(item: RegionalParcelDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is stale")
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelDraftRequiredCodeRule(var message: String) : RegionalParcelDraftRule {
    override fun check(item: RegionalParcelDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelDraftRetriesRangeRule(var min: Int, var max: Int) : RegionalParcelDraftRule {
    override fun check(item: RegionalParcelDraft): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelDraftCodePrefixRule(var prefix: String) : RegionalParcelDraftRule {
    override fun check(item: RegionalParcelDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional parcel draft. */
data class RegionalParcelDraftValidator(var rules: MutableList<RegionalParcelDraftRule>) {
    fun validate(item: RegionalParcelDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalParcelDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalParcelDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalParcelDraftValidator(): RegionalParcelDraftValidator {
    return RegionalParcelDraftValidator(
        mutableListOf<RegionalParcelDraftRule>(
            RegionalParcelDraftAllowedOwnerIdRule(mutableListOf<String>()),
            RegionalParcelDraftRequiredCodeRule("regional parcel draft needs a value"),
            RegionalParcelDraftRetriesRangeRule(120, 86400),
            RegionalParcelDraftCodePrefixRule("REF"),
        ),
    )
}
