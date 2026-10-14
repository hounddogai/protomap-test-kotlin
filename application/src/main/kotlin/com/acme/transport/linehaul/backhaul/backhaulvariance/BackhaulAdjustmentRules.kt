package com.acme.transport.linehaul.backhaul.backhaulvariance

/** A rule a backhaul adjustment must pass. Check returns its problems, or none. */
interface BackhaulAdjustmentRule {
    fun check(item: BackhaulAdjustment): MutableList<String>
}

data class BackhaulAdjustmentLaneLengthRule(var maxLength: Int) : BackhaulAdjustmentRule {
    override fun check(item: BackhaulAdjustment): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulAdjustmentCodeLengthRule(var maxLength: Int) : BackhaulAdjustmentRule {
    override fun check(item: BackhaulAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulAdjustmentNameLengthRule(var maxLength: Int) : BackhaulAdjustmentRule {
    override fun check(item: BackhaulAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul adjustment. */
data class BackhaulAdjustmentValidator(var rules: MutableList<BackhaulAdjustmentRule>) {
    fun validate(item: BackhaulAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BackhaulAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulAdjustmentValidator(): BackhaulAdjustmentValidator {
    return BackhaulAdjustmentValidator(
        mutableListOf<BackhaulAdjustmentRule>(
            BackhaulAdjustmentLaneLengthRule(64),
            BackhaulAdjustmentCodeLengthRule(64),
            BackhaulAdjustmentNameLengthRule(80),
        ),
    )
}
