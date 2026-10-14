package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.kit.text.normalize

/** A rule a capped accessorial entry must pass. Check returns its problems, or none. */
interface CappedAccessorialEntryRule {
    fun check(item: CappedAccessorialEntry): MutableList<String>
}

data class CappedAccessorialEntryRequiredSourceRule(var message: String) : CappedAccessorialEntryRule {
    override fun check(item: CappedAccessorialEntry): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedAccessorialEntryAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : CappedAccessorialEntryRule {
    override fun check(item: CappedAccessorialEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class CappedAccessorialEntryOwnerIdLengthRule(var maxLength: Int) : CappedAccessorialEntryRule {
    override fun check(item: CappedAccessorialEntry): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped accessorial entry. */
data class CappedAccessorialEntryValidator(var rules: MutableList<CappedAccessorialEntryRule>) {
    fun validate(item: CappedAccessorialEntry): MutableList<String> {
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

    fun requireValid(item: CappedAccessorialEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedAccessorialEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedAccessorialEntryValidator(): CappedAccessorialEntryValidator {
    return CappedAccessorialEntryValidator(
        mutableListOf<CappedAccessorialEntryRule>(
            CappedAccessorialEntryRequiredSourceRule("capped accessorial entry needs this field"),
            CappedAccessorialEntryAllowedOwnerIdRule(mutableListOf<String>()),
            CappedAccessorialEntryOwnerIdLengthRule(80),
        ),
    )
}
