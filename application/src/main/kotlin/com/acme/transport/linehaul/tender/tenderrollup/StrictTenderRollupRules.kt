package com.acme.transport.linehaul.tender.tenderrollup

/** A rule a strict tender rollup must pass. Check returns its problems, or none. */
interface StrictTenderRollupRule {
    fun check(item: StrictTenderRollup): MutableList<String>
}

data class StrictTenderRollupRequiredLabelRule(var message: String) : StrictTenderRollupRule {
    override fun check(item: StrictTenderRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTenderRollupCodeLengthRule(var maxLength: Int) : StrictTenderRollupRule {
    override fun check(item: StrictTenderRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTenderRollupLabelLengthRule(var maxLength: Int) : StrictTenderRollupRule {
    override fun check(item: StrictTenderRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTenderRollupAmountRangeRule(var min: Double, var max: Double) : StrictTenderRollupRule {
    override fun check(item: StrictTenderRollup): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class StrictTenderRollupExpiresAtRangeRule(var min: Int, var max: Int) : StrictTenderRollupRule {
    override fun check(item: StrictTenderRollup): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictTenderRollupCodePrefixRule(var prefix: String) : StrictTenderRollupRule {
    override fun check(item: StrictTenderRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict tender rollup. */
data class StrictTenderRollupValidator(var rules: MutableList<StrictTenderRollupRule>) {
    fun validate(item: StrictTenderRollup): MutableList<String> {
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

    fun requireValid(item: StrictTenderRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTenderRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTenderRollupValidator(): StrictTenderRollupValidator {
    return StrictTenderRollupValidator(
        mutableListOf<StrictTenderRollupRule>(
            StrictTenderRollupRequiredLabelRule("strict tender rollup needs a value"),
            StrictTenderRollupCodeLengthRule(1000),
            StrictTenderRollupLabelLengthRule(1000),
            StrictTenderRollupAmountRangeRule(8.4, 2276.9),
            StrictTenderRollupExpiresAtRangeRule(64, 255),
            StrictTenderRollupCodePrefixRule("Q"),
        ),
    )
}
