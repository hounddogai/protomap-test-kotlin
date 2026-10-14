package com.acme.transport.emissions.fuelburn.plan

/** A rule a scheduled fuel burn plan must pass. Check returns its problems, or none. */
interface ScheduledFuelBurnPlanRule {
    fun check(item: ScheduledFuelBurnPlan): MutableList<String>
}

data class ScheduledFuelBurnPlanLabelLengthRule(var maxLength: Int) : ScheduledFuelBurnPlanRule {
    override fun check(item: ScheduledFuelBurnPlan): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelBurnPlanBatchSizeRangeRule(var min: Int, var max: Int) : ScheduledFuelBurnPlanRule {
    override fun check(item: ScheduledFuelBurnPlan): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class ScheduledFuelBurnPlanDepotLengthRule(var maxLength: Int) : ScheduledFuelBurnPlanRule {
    override fun check(item: ScheduledFuelBurnPlan): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelBurnPlanRequiredDepotRule(var message: String) : ScheduledFuelBurnPlanRule {
    override fun check(item: ScheduledFuelBurnPlan): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelBurnPlanEtaMinutesRangeRule(var min: Int, var max: Int) : ScheduledFuelBurnPlanRule {
    override fun check(item: ScheduledFuelBurnPlan): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled fuel burn plan. */
data class ScheduledFuelBurnPlanValidator(var rules: MutableList<ScheduledFuelBurnPlanRule>) {
    fun validate(item: ScheduledFuelBurnPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledFuelBurnPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledFuelBurnPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledFuelBurnPlanValidator(): ScheduledFuelBurnPlanValidator {
    return ScheduledFuelBurnPlanValidator(
        mutableListOf<ScheduledFuelBurnPlanRule>(
            ScheduledFuelBurnPlanLabelLengthRule(86400),
            ScheduledFuelBurnPlanBatchSizeRangeRule(80, 80),
            ScheduledFuelBurnPlanDepotLengthRule(120),
            ScheduledFuelBurnPlanRequiredDepotRule("scheduled fuel burn plan needs this field"),
            ScheduledFuelBurnPlanEtaMinutesRangeRule(32, 80),
        ),
    )
}
