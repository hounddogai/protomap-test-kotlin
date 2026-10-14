package com.acme.transport.fleetops.defect.defectprofile

/** A rule a seasonal defect entry must pass. Check returns its problems, or none. */
interface SeasonalDefectEntryRule {
    fun check(item: SeasonalDefectEntry): MutableList<String>
}

data class SeasonalDefectEntryRequiredTitleRule(var message: String) : SeasonalDefectEntryRule {
    override fun check(item: SeasonalDefectEntry): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDefectEntryRequiredRouteCodeRule(var message: String) : SeasonalDefectEntryRule {
    override fun check(item: SeasonalDefectEntry): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDefectEntryTitleLengthRule(var maxLength: Int) : SeasonalDefectEntryRule {
    override fun check(item: SeasonalDefectEntry): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDefectEntryRequiredCodeRule(var message: String) : SeasonalDefectEntryRule {
    override fun check(item: SeasonalDefectEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal defect entry. */
data class SeasonalDefectEntryValidator(var rules: MutableList<SeasonalDefectEntryRule>) {
    fun validate(item: SeasonalDefectEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalDefectEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDefectEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDefectEntryValidator(): SeasonalDefectEntryValidator {
    return SeasonalDefectEntryValidator(
        mutableListOf<SeasonalDefectEntryRule>(
            SeasonalDefectEntryRequiredTitleRule("seasonal defect entry needs a value"),
            SeasonalDefectEntryRequiredRouteCodeRule("seasonal defect entry needs a value"),
            SeasonalDefectEntryTitleLengthRule(1000),
            SeasonalDefectEntryRequiredCodeRule("seasonal defect entry needs a value"),
        ),
    )
}
