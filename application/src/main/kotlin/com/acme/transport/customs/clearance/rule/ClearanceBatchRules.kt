package com.acme.transport.customs.clearance.rule

/** A rule a clearance batch must pass. Check returns its problems, or none. */
interface ClearanceBatchRule {
    fun check(item: ClearanceBatch): MutableList<String>
}

data class ClearanceBatchPalletsRangeRule(var min: Int, var max: Int) : ClearanceBatchRule {
    override fun check(item: ClearanceBatch): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceBatchVisibleGuardRule(var limit: Int) : ClearanceBatchRule {
    override fun check(item: ClearanceBatch): MutableList<String> {
        if (item.visible && item.pallets > limit) {
            return mutableListOf<String>("pallets ${item.pallets} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceBatchRevisionRangeRule(var min: Int, var max: Int) : ClearanceBatchRule {
    override fun check(item: ClearanceBatch): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance batch. */
data class ClearanceBatchValidator(var rules: MutableList<ClearanceBatchRule>) {
    fun validate(item: ClearanceBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearanceBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceBatchValidator(): ClearanceBatchValidator {
    return ClearanceBatchValidator(
        mutableListOf<ClearanceBatchRule>(
            ClearanceBatchPalletsRangeRule(16, 64),
            ClearanceBatchVisibleGuardRule(16),
            ClearanceBatchRevisionRangeRule(64, 80),
        ),
    )
}
