package com.acme.transport.linehaul.tender.tier

/** A rule a manual tender rollup must pass. Check returns its problems, or none. */
interface ManualTenderRollupRule {
    fun check(item: ManualTenderRollup): MutableList<String>
}

data class ManualTenderRollupTitleLengthRule(var maxLength: Int) : ManualTenderRollupRule {
    override fun check(item: ManualTenderRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderRollupCapacityRangeRule(var min: Int, var max: Int) : ManualTenderRollupRule {
    override fun check(item: ManualTenderRollup): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class ManualTenderRollupCodeLengthRule(var maxLength: Int) : ManualTenderRollupRule {
    override fun check(item: ManualTenderRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual tender rollup. */
data class ManualTenderRollupValidator(var rules: MutableList<ManualTenderRollupRule>) {
    fun validate(item: ManualTenderRollup): MutableList<String> {
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

    fun requireValid(item: ManualTenderRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTenderRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTenderRollupValidator(): ManualTenderRollupValidator {
    return ManualTenderRollupValidator(
        mutableListOf<ManualTenderRollupRule>(
            ManualTenderRollupTitleLengthRule(80),
            ManualTenderRollupCapacityRangeRule(32, 120),
            ManualTenderRollupCodeLengthRule(16),
        ),
    )
}
