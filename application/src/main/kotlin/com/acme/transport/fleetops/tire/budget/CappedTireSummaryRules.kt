package com.acme.transport.fleetops.tire.budget

/** A rule a capped tire summary must pass. Check returns its problems, or none. */
interface CappedTireSummaryRule {
    fun check(item: CappedTireSummary): MutableList<String>
}

data class CappedTireSummaryRequiredCodeRule(var message: String) : CappedTireSummaryRule {
    override fun check(item: CappedTireSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedTireSummaryUpdatedAtRangeRule(var min: Int, var max: Int) : CappedTireSummaryRule {
    override fun check(item: CappedTireSummary): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedTireSummaryUnitLengthRule(var maxLength: Int) : CappedTireSummaryRule {
    override fun check(item: CappedTireSummary): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped tire summary. */
data class CappedTireSummaryValidator(var rules: MutableList<CappedTireSummaryRule>) {
    fun validate(item: CappedTireSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedTireSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTireSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTireSummaryValidator(): CappedTireSummaryValidator {
    return CappedTireSummaryValidator(
        mutableListOf<CappedTireSummaryRule>(
            CappedTireSummaryRequiredCodeRule("capped tire summary needs an entry"),
            CappedTireSummaryUpdatedAtRangeRule(32, 255),
            CappedTireSummaryUnitLengthRule(80),
        ),
    )
}
