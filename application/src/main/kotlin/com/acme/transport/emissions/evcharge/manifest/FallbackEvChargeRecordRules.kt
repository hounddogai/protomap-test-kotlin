package com.acme.transport.emissions.evcharge.manifest

import com.acme.transport.kit.text.normalize

/** A rule a fallback ev charge record must pass. Check returns its problems, or none. */
interface FallbackEvChargeRecordRule {
    fun check(item: FallbackEvChargeRecord): MutableList<String>
}

data class FallbackEvChargeRecordRequiredCodeRule(var message: String) : FallbackEvChargeRecordRule {
    override fun check(item: FallbackEvChargeRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackEvChargeRecordDescriptionLengthRule(var maxLength: Int) : FallbackEvChargeRecordRule {
    override fun check(item: FallbackEvChargeRecord): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEvChargeRecordCodePrefixRule(var prefix: String) : FallbackEvChargeRecordRule {
    override fun check(item: FallbackEvChargeRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEvChargeRecordAllowedCurrencyRule(
    var allowed: MutableList<String>,
) : FallbackEvChargeRecordRule {
    override fun check(item: FallbackEvChargeRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEvChargeRecordRequiredChannelRule(var message: String) : FallbackEvChargeRecordRule {
    override fun check(item: FallbackEvChargeRecord): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackEvChargeRecordCodeLengthRule(var maxLength: Int) : FallbackEvChargeRecordRule {
    override fun check(item: FallbackEvChargeRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback ev charge record. */
data class FallbackEvChargeRecordValidator(var rules: MutableList<FallbackEvChargeRecordRule>) {
    fun validate(item: FallbackEvChargeRecord): MutableList<String> {
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

    fun requireValid(item: FallbackEvChargeRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackEvChargeRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackEvChargeRecordValidator(): FallbackEvChargeRecordValidator {
    return FallbackEvChargeRecordValidator(
        mutableListOf<FallbackEvChargeRecordRule>(
            FallbackEvChargeRecordRequiredCodeRule("fallback ev charge record needs this field"),
            FallbackEvChargeRecordDescriptionLengthRule(80),
            FallbackEvChargeRecordCodePrefixRule("REF"),
            FallbackEvChargeRecordAllowedCurrencyRule(mutableListOf<String>("pending")),
            FallbackEvChargeRecordRequiredChannelRule("fallback ev charge record needs this field"),
            FallbackEvChargeRecordCodeLengthRule(16),
        ),
    )
}
