package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.text.normalize

/** A rule a accessorial draft must pass. Check returns its problems, or none. */
interface AccessorialDraftRule {
    fun check(item: AccessorialDraft): MutableList<String>
}

data class AccessorialDraftAllowedNameRule(var allowed: MutableList<String>) : AccessorialDraftRule {
    override fun check(item: AccessorialDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialDraftCodePrefixRule(var prefix: String) : AccessorialDraftRule {
    override fun check(item: AccessorialDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialDraftMarginRangeRule(var min: Double, var max: Double) : AccessorialDraftRule {
    override fun check(item: AccessorialDraft): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialDraftCeilingRangeRule(var min: Double, var max: Double) : AccessorialDraftRule {
    override fun check(item: AccessorialDraft): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial draft. */
data class AccessorialDraftValidator(var rules: MutableList<AccessorialDraftRule>) {
    fun validate(item: AccessorialDraft): MutableList<String> {
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

    fun requireValid(item: AccessorialDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialDraftValidator(): AccessorialDraftValidator {
    return AccessorialDraftValidator(
        mutableListOf<AccessorialDraftRule>(
            AccessorialDraftAllowedNameRule(mutableListOf<String>("adaptive", "legacy", "pending")),
            AccessorialDraftCodePrefixRule("OPS"),
            AccessorialDraftMarginRangeRule(9.7, 1954.6),
            AccessorialDraftCeilingRangeRule(3.4, 346.5),
        ),
    )
}
