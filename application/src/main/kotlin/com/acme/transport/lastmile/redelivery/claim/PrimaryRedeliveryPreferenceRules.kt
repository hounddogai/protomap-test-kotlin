package com.acme.transport.lastmile.redelivery.claim

import com.acme.transport.kit.text.normalize

/** A rule a primary redelivery preference must pass. Check returns its problems, or none. */
interface PrimaryRedeliveryPreferenceRule {
    fun check(item: PrimaryRedeliveryPreference): MutableList<String>
}

data class PrimaryRedeliveryPreferenceVisibleGuardRule(var limit: Int) : PrimaryRedeliveryPreferenceRule {
    override fun check(item: PrimaryRedeliveryPreference): MutableList<String> {
        if (item.visible && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRedeliveryPreferenceAllowedNameRule(
    var allowed: MutableList<String>,
) : PrimaryRedeliveryPreferenceRule {
    override fun check(item: PrimaryRedeliveryPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRedeliveryPreferenceNameLengthRule(var maxLength: Int) : PrimaryRedeliveryPreferenceRule {
    override fun check(item: PrimaryRedeliveryPreference): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRedeliveryPreferenceRequiredNameRule(var message: String) : PrimaryRedeliveryPreferenceRule {
    override fun check(item: PrimaryRedeliveryPreference): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRedeliveryPreferenceCodePrefixRule(var prefix: String) : PrimaryRedeliveryPreferenceRule {
    override fun check(item: PrimaryRedeliveryPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary redelivery preference. */
data class PrimaryRedeliveryPreferenceValidator(var rules: MutableList<PrimaryRedeliveryPreferenceRule>) {
    fun validate(item: PrimaryRedeliveryPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryRedeliveryPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryRedeliveryPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryRedeliveryPreferenceValidator(): PrimaryRedeliveryPreferenceValidator {
    return PrimaryRedeliveryPreferenceValidator(
        mutableListOf<PrimaryRedeliveryPreferenceRule>(
            PrimaryRedeliveryPreferenceVisibleGuardRule(86400),
            PrimaryRedeliveryPreferenceAllowedNameRule(mutableListOf<String>("batched", "manual")),
            PrimaryRedeliveryPreferenceNameLengthRule(64),
            PrimaryRedeliveryPreferenceRequiredNameRule("primary redelivery preference needs an entry"),
            PrimaryRedeliveryPreferenceCodePrefixRule("Q"),
        ),
    )
}
