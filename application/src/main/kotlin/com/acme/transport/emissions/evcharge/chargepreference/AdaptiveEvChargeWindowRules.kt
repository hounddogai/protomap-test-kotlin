package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.kit.text.normalize

/** A rule a adaptive ev charge window must pass. Check returns its problems, or none. */
interface AdaptiveEvChargeWindowRule {
    fun check(item: AdaptiveEvChargeWindow): MutableList<String>
}

data class AdaptiveEvChargeWindowReviewerLengthRule(var maxLength: Int) : AdaptiveEvChargeWindowRule {
    override fun check(item: AdaptiveEvChargeWindow): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeWindowAllowedNameRule(
    var allowed: MutableList<String>,
) : AdaptiveEvChargeWindowRule {
    override fun check(item: AdaptiveEvChargeWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeWindowCodePrefixRule(var prefix: String) : AdaptiveEvChargeWindowRule {
    override fun check(item: AdaptiveEvChargeWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeWindowRequiredReviewerRule(var message: String) : AdaptiveEvChargeWindowRule {
    override fun check(item: AdaptiveEvChargeWindow): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeWindowRequiredNameRule(var message: String) : AdaptiveEvChargeWindowRule {
    override fun check(item: AdaptiveEvChargeWindow): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeWindowNameLengthRule(var maxLength: Int) : AdaptiveEvChargeWindowRule {
    override fun check(item: AdaptiveEvChargeWindow): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive ev charge window. */
data class AdaptiveEvChargeWindowValidator(var rules: MutableList<AdaptiveEvChargeWindowRule>) {
    fun validate(item: AdaptiveEvChargeWindow): MutableList<String> {
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

    fun requireValid(item: AdaptiveEvChargeWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveEvChargeWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveEvChargeWindowValidator(): AdaptiveEvChargeWindowValidator {
    return AdaptiveEvChargeWindowValidator(
        mutableListOf<AdaptiveEvChargeWindowRule>(
            AdaptiveEvChargeWindowReviewerLengthRule(32),
            AdaptiveEvChargeWindowAllowedNameRule(mutableListOf<String>("tiered", "adaptive", "scheduled")),
            AdaptiveEvChargeWindowCodePrefixRule("OPS"),
            AdaptiveEvChargeWindowRequiredReviewerRule("adaptive ev charge window needs this field"),
            AdaptiveEvChargeWindowRequiredNameRule("adaptive ev charge window needs an entry"),
            AdaptiveEvChargeWindowNameLengthRule(3600),
        ),
    )
}
