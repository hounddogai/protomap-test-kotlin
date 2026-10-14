package com.acme.transport.linehaul.laneplan.budget

/** A rule a lane plan summary must pass. Check returns its problems, or none. */
interface LanePlanSummaryRule {
    fun check(item: LanePlanSummary): MutableList<String>
}

data class LanePlanSummaryContactEmailLengthRule(var maxLength: Int) : LanePlanSummaryRule {
    override fun check(item: LanePlanSummary): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanSummaryRequiredCodeRule(var message: String) : LanePlanSummaryRule {
    override fun check(item: LanePlanSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanSummaryNameLengthRule(var maxLength: Int) : LanePlanSummaryRule {
    override fun check(item: LanePlanSummary): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanSummaryWindowDaysRangeRule(var min: Int, var max: Int) : LanePlanSummaryRule {
    override fun check(item: LanePlanSummary): MutableList<String> {
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

/** Runs every rule of a lane plan summary. */
data class LanePlanSummaryValidator(var rules: MutableList<LanePlanSummaryRule>) {
    fun validate(item: LanePlanSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LanePlanSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanSummaryValidator(): LanePlanSummaryValidator {
    return LanePlanSummaryValidator(
        mutableListOf<LanePlanSummaryRule>(
            LanePlanSummaryContactEmailLengthRule(64),
            LanePlanSummaryRequiredCodeRule("lane plan summary needs this field"),
            LanePlanSummaryNameLengthRule(80),
            LanePlanSummaryWindowDaysRangeRule(32, 3600),
        ),
    )
}
