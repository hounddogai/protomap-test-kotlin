package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.text.normalize

/** A rule a cutoff tier must pass. Check returns its problems, or none. */
interface CutoffTierRule {
    fun check(item: CutoffTier): MutableList<String>
}

data class CutoffTierLabelLengthRule(var maxLength: Int) : CutoffTierRule {
    override fun check(item: CutoffTier): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffTierDurationMinutesRangeRule(var min: Int, var max: Int) : CutoffTierRule {
    override fun check(item: CutoffTier): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class CutoffTierCodePrefixRule(var prefix: String) : CutoffTierRule {
    override fun check(item: CutoffTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CutoffTierRequiredLabelRule(var message: String) : CutoffTierRule {
    override fun check(item: CutoffTier): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffTierAllowedNameRule(var allowed: MutableList<String>) : CutoffTierRule {
    override fun check(item: CutoffTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class CutoffTierCodeLengthRule(var maxLength: Int) : CutoffTierRule {
    override fun check(item: CutoffTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff tier. */
data class CutoffTierValidator(var rules: MutableList<CutoffTierRule>) {
    fun validate(item: CutoffTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CutoffTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffTier>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffTierValidator(): CutoffTierValidator {
    return CutoffTierValidator(
        mutableListOf<CutoffTierRule>(
            CutoffTierLabelLengthRule(80),
            CutoffTierDurationMinutesRangeRule(16, 80),
            CutoffTierCodePrefixRule("Q"),
            CutoffTierRequiredLabelRule("cutoff tier needs a value"),
            CutoffTierAllowedNameRule(mutableListOf<String>("rolling")),
            CutoffTierCodeLengthRule(64),
        ),
    )
}
