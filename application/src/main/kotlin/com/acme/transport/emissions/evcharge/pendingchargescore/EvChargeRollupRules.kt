package com.acme.transport.emissions.evcharge.pendingchargescore

/** A rule a ev charge rollup must pass. Check returns its problems, or none. */
interface EvChargeRollupRule {
    fun check(item: EvChargeRollup): MutableList<String>
}

data class EvChargeRollupCodeLengthRule(var maxLength: Int) : EvChargeRollupRule {
    override fun check(item: EvChargeRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeRollupRequiredNameRule(var message: String) : EvChargeRollupRule {
    override fun check(item: EvChargeRollup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeRollupEndsAtRangeRule(var min: Int, var max: Int) : EvChargeRollupRule {
    override fun check(item: EvChargeRollup): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a ev charge rollup. */
data class EvChargeRollupValidator(var rules: MutableList<EvChargeRollupRule>) {
    fun validate(item: EvChargeRollup): MutableList<String> {
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

    fun requireValid(item: EvChargeRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeRollupValidator(): EvChargeRollupValidator {
    return EvChargeRollupValidator(
        mutableListOf<EvChargeRollupRule>(
            EvChargeRollupCodeLengthRule(120),
            EvChargeRollupRequiredNameRule("ev charge rollup needs an entry"),
            EvChargeRollupEndsAtRangeRule(80, 255),
        ),
    )
}
