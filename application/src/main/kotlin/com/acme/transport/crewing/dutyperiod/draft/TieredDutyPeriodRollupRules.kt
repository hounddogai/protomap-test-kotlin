package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.kit.text.normalize

/** A rule a tiered duty period rollup must pass. Check returns its problems, or none. */
interface TieredDutyPeriodRollupRule {
    fun check(item: TieredDutyPeriodRollup): MutableList<String>
}

data class TieredDutyPeriodRollupTitleLengthRule(var maxLength: Int) : TieredDutyPeriodRollupRule {
    override fun check(item: TieredDutyPeriodRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDutyPeriodRollupAllowedCodeRule(
    var allowed: MutableList<String>,
) : TieredDutyPeriodRollupRule {
    override fun check(item: TieredDutyPeriodRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class TieredDutyPeriodRollupDescriptionLengthRule(var maxLength: Int) : TieredDutyPeriodRollupRule {
    override fun check(item: TieredDutyPeriodRollup): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDutyPeriodRollupRequiredDescriptionRule(var message: String) : TieredDutyPeriodRollupRule {
    override fun check(item: TieredDutyPeriodRollup): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDutyPeriodRollupCodePrefixRule(var prefix: String) : TieredDutyPeriodRollupRule {
    override fun check(item: TieredDutyPeriodRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredDutyPeriodRollupRetriesRangeRule(var min: Int, var max: Int) : TieredDutyPeriodRollupRule {
    override fun check(item: TieredDutyPeriodRollup): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered duty period rollup. */
data class TieredDutyPeriodRollupValidator(var rules: MutableList<TieredDutyPeriodRollupRule>) {
    fun validate(item: TieredDutyPeriodRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredDutyPeriodRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDutyPeriodRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDutyPeriodRollupValidator(): TieredDutyPeriodRollupValidator {
    return TieredDutyPeriodRollupValidator(
        mutableListOf<TieredDutyPeriodRollupRule>(
            TieredDutyPeriodRollupTitleLengthRule(255),
            TieredDutyPeriodRollupAllowedCodeRule(mutableListOf<String>("manual")),
            TieredDutyPeriodRollupDescriptionLengthRule(32),
            TieredDutyPeriodRollupRequiredDescriptionRule("tiered duty period rollup needs an entry"),
            TieredDutyPeriodRollupCodePrefixRule("REF"),
            TieredDutyPeriodRollupRetriesRangeRule(64, 255),
        ),
    )
}
