package com.acme.transport.fleetops.utilization.rollup

/** A rule a manual utilization summary must pass. Check returns its problems, or none. */
interface ManualUtilizationSummaryRule {
    fun check(item: ManualUtilizationSummary): MutableList<String>
}

data class ManualUtilizationSummaryTimezoneLengthRule(var maxLength: Int) : ManualUtilizationSummaryRule {
    override fun check(item: ManualUtilizationSummary): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationSummaryTrailerIdLengthRule(var maxLength: Int) : ManualUtilizationSummaryRule {
    override fun check(item: ManualUtilizationSummary): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationSummaryRequiredTrailerIdRule(var message: String) : ManualUtilizationSummaryRule {
    override fun check(item: ManualUtilizationSummary): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationSummaryRequiredNameRule(var message: String) : ManualUtilizationSummaryRule {
    override fun check(item: ManualUtilizationSummary): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual utilization summary. */
data class ManualUtilizationSummaryValidator(var rules: MutableList<ManualUtilizationSummaryRule>) {
    fun validate(item: ManualUtilizationSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualUtilizationSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualUtilizationSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultManualUtilizationSummaryValidator(): ManualUtilizationSummaryValidator {
    return ManualUtilizationSummaryValidator(
        mutableListOf<ManualUtilizationSummaryRule>(
            ManualUtilizationSummaryTimezoneLengthRule(1000),
            ManualUtilizationSummaryTrailerIdLengthRule(80),
            ManualUtilizationSummaryRequiredTrailerIdRule("manual utilization summary needs a value"),
            ManualUtilizationSummaryRequiredNameRule("manual utilization summary needs an entry"),
        ),
    )
}
