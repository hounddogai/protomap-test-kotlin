package com.acme.transport.fleetops.defect.defectproposal

/** A rule a tiered defect variance must pass. Check returns its problems, or none. */
interface TieredDefectVarianceRule {
    fun check(item: TieredDefectVariance): MutableList<String>
}

data class TieredDefectVarianceRevisionRangeRule(var min: Int, var max: Int) : TieredDefectVarianceRule {
    override fun check(item: TieredDefectVariance): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class TieredDefectVarianceRequiredLabelRule(var message: String) : TieredDefectVarianceRule {
    override fun check(item: TieredDefectVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDefectVarianceRequiredCodeRule(var message: String) : TieredDefectVarianceRule {
    override fun check(item: TieredDefectVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered defect variance. */
data class TieredDefectVarianceValidator(var rules: MutableList<TieredDefectVarianceRule>) {
    fun validate(item: TieredDefectVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredDefectVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDefectVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDefectVarianceValidator(): TieredDefectVarianceValidator {
    return TieredDefectVarianceValidator(
        mutableListOf<TieredDefectVarianceRule>(
            TieredDefectVarianceRevisionRangeRule(86400, 86400),
            TieredDefectVarianceRequiredLabelRule("tiered defect variance needs an entry"),
            TieredDefectVarianceRequiredCodeRule("tiered defect variance needs a value"),
        ),
    )
}
