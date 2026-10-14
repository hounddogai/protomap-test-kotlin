package com.acme.transport.emissions.emissionfactor.notice

import com.acme.transport.kit.text.normalize

/** A rule a emission factor notice must pass. Check returns its problems, or none. */
interface EmissionFactorNoticeRule {
    fun check(item: EmissionFactorNotice): MutableList<String>
}

data class EmissionFactorNoticeWindowDaysRangeRule(var min: Int, var max: Int) : EmissionFactorNoticeRule {
    override fun check(item: EmissionFactorNotice): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class EmissionFactorNoticeCodeLengthRule(var maxLength: Int) : EmissionFactorNoticeRule {
    override fun check(item: EmissionFactorNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorNoticeCodePrefixRule(var prefix: String) : EmissionFactorNoticeRule {
    override fun check(item: EmissionFactorNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorNoticeRequiredCodeRule(var message: String) : EmissionFactorNoticeRule {
    override fun check(item: EmissionFactorNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorNoticeLabelLengthRule(var maxLength: Int) : EmissionFactorNoticeRule {
    override fun check(item: EmissionFactorNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorNoticeAllowedLabelRule(var allowed: MutableList<String>) : EmissionFactorNoticeRule {
    override fun check(item: EmissionFactorNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor notice. */
data class EmissionFactorNoticeValidator(var rules: MutableList<EmissionFactorNoticeRule>) {
    fun validate(item: EmissionFactorNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorNoticeValidator(): EmissionFactorNoticeValidator {
    return EmissionFactorNoticeValidator(
        mutableListOf<EmissionFactorNoticeRule>(
            EmissionFactorNoticeWindowDaysRangeRule(16, 80),
            EmissionFactorNoticeCodeLengthRule(3600),
            EmissionFactorNoticeCodePrefixRule("X"),
            EmissionFactorNoticeRequiredCodeRule("emission factor notice needs a value"),
            EmissionFactorNoticeLabelLengthRule(32),
            EmissionFactorNoticeAllowedLabelRule(mutableListOf<String>("strict", "primary")),
        ),
    )
}
