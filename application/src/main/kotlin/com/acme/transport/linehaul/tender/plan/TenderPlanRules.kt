package com.acme.transport.linehaul.tender.plan

/** A rule a tender plan must pass. Check returns its problems, or none. */
interface TenderPlanRule {
    fun check(item: TenderPlan): MutableList<String>
}

data class TenderPlanRequiredCodeRule(var message: String) : TenderPlanRule {
    override fun check(item: TenderPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderPlanLaneLengthRule(var maxLength: Int) : TenderPlanRule {
    override fun check(item: TenderPlan): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderPlanTitleLengthRule(var maxLength: Int) : TenderPlanRule {
    override fun check(item: TenderPlan): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender plan. */
data class TenderPlanValidator(var rules: MutableList<TenderPlanRule>) {
    fun validate(item: TenderPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderPlanValidator(): TenderPlanValidator {
    return TenderPlanValidator(
        mutableListOf<TenderPlanRule>(
            TenderPlanRequiredCodeRule("tender plan needs a value"),
            TenderPlanLaneLengthRule(32),
            TenderPlanTitleLengthRule(3600),
        ),
    )
}
