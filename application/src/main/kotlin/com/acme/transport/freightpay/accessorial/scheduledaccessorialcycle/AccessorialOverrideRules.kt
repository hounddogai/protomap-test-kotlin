package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.kit.text.normalize

/** A rule a accessorial override must pass. Check returns its problems, or none. */
interface AccessorialOverrideRule {
    fun check(item: AccessorialOverride): MutableList<String>
}

data class AccessorialOverrideTitleLengthRule(var maxLength: Int) : AccessorialOverrideRule {
    override fun check(item: AccessorialOverride): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialOverrideCodeLengthRule(var maxLength: Int) : AccessorialOverrideRule {
    override fun check(item: AccessorialOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialOverridePinnedGuardRule(var limit: Double) : AccessorialOverrideRule {
    override fun check(item: AccessorialOverride): MutableList<String> {
        if (item.pinned && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialOverrideThresholdRangeRule(var min: Double, var max: Double) : AccessorialOverrideRule {
    override fun check(item: AccessorialOverride): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialOverrideAllowedTitleRule(var allowed: MutableList<String>) : AccessorialOverrideRule {
    override fun check(item: AccessorialOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialOverrideRequiredCodeRule(var message: String) : AccessorialOverrideRule {
    override fun check(item: AccessorialOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial override. */
data class AccessorialOverrideValidator(var rules: MutableList<AccessorialOverrideRule>) {
    fun validate(item: AccessorialOverride): MutableList<String> {
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

    fun requireValid(item: AccessorialOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialOverrideValidator(): AccessorialOverrideValidator {
    return AccessorialOverrideValidator(
        mutableListOf<AccessorialOverrideRule>(
            AccessorialOverrideTitleLengthRule(64),
            AccessorialOverrideCodeLengthRule(255),
            AccessorialOverridePinnedGuardRule(2.1),
            AccessorialOverrideThresholdRangeRule(4.8, 4517.2),
            AccessorialOverrideAllowedTitleRule(mutableListOf<String>("seasonal", "capped", "strict")),
            AccessorialOverrideRequiredCodeRule("accessorial override needs this field"),
        ),
    )
}
