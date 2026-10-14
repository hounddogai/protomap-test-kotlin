package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

/** A rule a regional trailer batch must pass. Check returns its problems, or none. */
interface RegionalTrailerBatchRule {
    fun check(item: RegionalTrailerBatch): MutableList<String>
}

data class RegionalTrailerBatchTitleLengthRule(var maxLength: Int) : RegionalTrailerBatchRule {
    override fun check(item: RegionalTrailerBatch): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerBatchRequiredCodeRule(var message: String) : RegionalTrailerBatchRule {
    override fun check(item: RegionalTrailerBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerBatchRequiredRegionRule(var message: String) : RegionalTrailerBatchRule {
    override fun check(item: RegionalTrailerBatch): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerBatchRequiredUnitRule(var message: String) : RegionalTrailerBatchRule {
    override fun check(item: RegionalTrailerBatch): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional trailer batch. */
data class RegionalTrailerBatchValidator(var rules: MutableList<RegionalTrailerBatchRule>) {
    fun validate(item: RegionalTrailerBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalTrailerBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalTrailerBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalTrailerBatchValidator(): RegionalTrailerBatchValidator {
    return RegionalTrailerBatchValidator(
        mutableListOf<RegionalTrailerBatchRule>(
            RegionalTrailerBatchTitleLengthRule(120),
            RegionalTrailerBatchRequiredCodeRule("regional trailer batch needs a value"),
            RegionalTrailerBatchRequiredRegionRule("regional trailer batch needs this field"),
            RegionalTrailerBatchRequiredUnitRule("regional trailer batch needs an entry"),
        ),
    )
}
