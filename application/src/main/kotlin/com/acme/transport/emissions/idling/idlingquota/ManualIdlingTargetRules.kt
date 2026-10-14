package com.acme.transport.emissions.idling.idlingquota

import com.acme.transport.kit.text.normalize

/** A rule a manual idling target must pass. Check returns its problems, or none. */
interface ManualIdlingTargetRule {
    fun check(item: ManualIdlingTarget): MutableList<String>
}

data class ManualIdlingTargetRequiredCodeRule(var message: String) : ManualIdlingTargetRule {
    override fun check(item: ManualIdlingTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualIdlingTargetCodePrefixRule(var prefix: String) : ManualIdlingTargetRule {
    override fun check(item: ManualIdlingTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualIdlingTargetRequiredReviewerRule(var message: String) : ManualIdlingTargetRule {
    override fun check(item: ManualIdlingTarget): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualIdlingTargetVisibleGuardRule(var limit: Int) : ManualIdlingTargetRule {
    override fun check(item: ManualIdlingTarget): MutableList<String> {
        if (item.visible && item.pallets > limit) {
            return mutableListOf<String>("pallets ${item.pallets} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ManualIdlingTargetAllowedReviewerRule(var allowed: MutableList<String>) : ManualIdlingTargetRule {
    override fun check(item: ManualIdlingTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class ManualIdlingTargetTitleLengthRule(var maxLength: Int) : ManualIdlingTargetRule {
    override fun check(item: ManualIdlingTarget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual idling target. */
data class ManualIdlingTargetValidator(var rules: MutableList<ManualIdlingTargetRule>) {
    fun validate(item: ManualIdlingTarget): MutableList<String> {
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

    fun requireValid(item: ManualIdlingTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualIdlingTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultManualIdlingTargetValidator(): ManualIdlingTargetValidator {
    return ManualIdlingTargetValidator(
        mutableListOf<ManualIdlingTargetRule>(
            ManualIdlingTargetRequiredCodeRule("manual idling target needs this field"),
            ManualIdlingTargetCodePrefixRule("ACM"),
            ManualIdlingTargetRequiredReviewerRule("manual idling target needs a value"),
            ManualIdlingTargetVisibleGuardRule(86400),
            ManualIdlingTargetAllowedReviewerRule(mutableListOf<String>("weighted")),
            ManualIdlingTargetTitleLengthRule(64),
        ),
    )
}
