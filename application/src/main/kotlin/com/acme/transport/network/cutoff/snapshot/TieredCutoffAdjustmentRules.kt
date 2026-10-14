package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a tiered cutoff adjustment must pass. Check returns its problems, or none. */
interface TieredCutoffAdjustmentRule {
    fun check(item: TieredCutoffAdjustment): MutableList<String>
}

data class TieredCutoffAdjustmentTitleLengthRule(var maxLength: Int) : TieredCutoffAdjustmentRule {
    override fun check(item: TieredCutoffAdjustment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCutoffAdjustmentAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : TieredCutoffAdjustmentRule {
    override fun check(item: TieredCutoffAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is expired")
        }
        return mutableListOf<String>()
    }
}

data class TieredCutoffAdjustmentLabelLengthRule(var maxLength: Int) : TieredCutoffAdjustmentRule {
    override fun check(item: TieredCutoffAdjustment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCutoffAdjustmentRequiredLabelRule(var message: String) : TieredCutoffAdjustmentRule {
    override fun check(item: TieredCutoffAdjustment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered cutoff adjustment. */
data class TieredCutoffAdjustmentValidator(var rules: MutableList<TieredCutoffAdjustmentRule>) {
    fun validate(item: TieredCutoffAdjustment): MutableList<String> {
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

    fun requireValid(item: TieredCutoffAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCutoffAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCutoffAdjustmentValidator(): TieredCutoffAdjustmentValidator {
    return TieredCutoffAdjustmentValidator(
        mutableListOf<TieredCutoffAdjustmentRule>(
            TieredCutoffAdjustmentTitleLengthRule(120),
            TieredCutoffAdjustmentAllowedTimezoneRule(mutableListOf<String>()),
            TieredCutoffAdjustmentLabelLengthRule(86400),
            TieredCutoffAdjustmentRequiredLabelRule("tiered cutoff adjustment needs a value"),
        ),
    )
}
