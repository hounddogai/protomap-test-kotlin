package com.acme.transport.network.cutoff.allocation

/** A rule a cutoff adjustment must pass. Check returns its problems, or none. */
interface CutoffAdjustmentRule {
    fun check(item: CutoffAdjustment): MutableList<String>
}

data class CutoffAdjustmentLoadKgRangeRule(var min: Double, var max: Double) : CutoffAdjustmentRule {
    override fun check(item: CutoffAdjustment): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class CutoffAdjustmentCodeLengthRule(var maxLength: Int) : CutoffAdjustmentRule {
    override fun check(item: CutoffAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffAdjustmentNameLengthRule(var maxLength: Int) : CutoffAdjustmentRule {
    override fun check(item: CutoffAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff adjustment. */
data class CutoffAdjustmentValidator(var rules: MutableList<CutoffAdjustmentRule>) {
    fun validate(item: CutoffAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CutoffAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffAdjustmentValidator(): CutoffAdjustmentValidator {
    return CutoffAdjustmentValidator(
        mutableListOf<CutoffAdjustmentRule>(
            CutoffAdjustmentLoadKgRangeRule(6.3, 968.9),
            CutoffAdjustmentCodeLengthRule(120),
            CutoffAdjustmentNameLengthRule(3600),
        ),
    )
}
