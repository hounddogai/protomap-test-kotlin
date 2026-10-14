package com.acme.transport.network.hub.mapping

import com.acme.transport.kit.text.normalize

/** A rule a hub exception must pass. Check returns its problems, or none. */
interface HubExceptionRule {
    fun check(item: HubException): MutableList<String>
}

data class HubExceptionRequiredCountryRule(var message: String) : HubExceptionRule {
    override fun check(item: HubException): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubExceptionAllowedReviewerRule(var allowed: MutableList<String>) : HubExceptionRule {
    override fun check(item: HubException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class HubExceptionBatchSizeRangeRule(var min: Int, var max: Int) : HubExceptionRule {
    override fun check(item: HubException): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class HubExceptionRequiredCodeRule(var message: String) : HubExceptionRule {
    override fun check(item: HubException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubExceptionCodeLengthRule(var maxLength: Int) : HubExceptionRule {
    override fun check(item: HubException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubExceptionCodePrefixRule(var prefix: String) : HubExceptionRule {
    override fun check(item: HubException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub exception. */
data class HubExceptionValidator(var rules: MutableList<HubExceptionRule>) {
    fun validate(item: HubException): MutableList<String> {
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

    fun requireValid(item: HubException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubException>): MutableMap<String, MutableList<String>> {
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

fun defaultHubExceptionValidator(): HubExceptionValidator {
    return HubExceptionValidator(
        mutableListOf<HubExceptionRule>(
            HubExceptionRequiredCountryRule("hub exception needs this field"),
            HubExceptionAllowedReviewerRule(mutableListOf<String>("fallback", "legacy")),
            HubExceptionBatchSizeRangeRule(255, 86400),
            HubExceptionRequiredCodeRule("hub exception needs an entry"),
            HubExceptionCodeLengthRule(32),
            HubExceptionCodePrefixRule("ACM"),
        ),
    )
}
