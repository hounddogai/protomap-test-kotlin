package com.acme.transport.safety.incident.incidentoverride

/** A rule a capped incident tier must pass. Check returns its problems, or none. */
interface CappedIncidentTierRule {
    fun check(item: CappedIncidentTier): MutableList<String>
}

data class CappedIncidentTierQuantityRangeRule(var min: Int, var max: Int) : CappedIncidentTierRule {
    override fun check(item: CappedIncidentTier): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedIncidentTierBudgetRangeRule(var min: Double, var max: Double) : CappedIncidentTierRule {
    override fun check(item: CappedIncidentTier): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedIncidentTierWindowDaysRangeRule(var min: Int, var max: Int) : CappedIncidentTierRule {
    override fun check(item: CappedIncidentTier): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped incident tier. */
data class CappedIncidentTierValidator(var rules: MutableList<CappedIncidentTierRule>) {
    fun validate(item: CappedIncidentTier): MutableList<String> {
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

    fun requireValid(item: CappedIncidentTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedIncidentTier>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedIncidentTierValidator(): CappedIncidentTierValidator {
    return CappedIncidentTierValidator(
        mutableListOf<CappedIncidentTierRule>(
            CappedIncidentTierQuantityRangeRule(16, 32),
            CappedIncidentTierBudgetRangeRule(7.9, 3357.2),
            CappedIncidentTierWindowDaysRangeRule(16, 255),
        ),
    )
}
