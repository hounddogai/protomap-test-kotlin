package com.acme.transport.linehaul.capacity.capacityassignment

/** A rule a primary capacity variance must pass. Check returns its problems, or none. */
interface PrimaryCapacityVarianceRule {
    fun check(item: PrimaryCapacityVariance): MutableList<String>
}

data class PrimaryCapacityVarianceRequiredSummaryRule(var message: String) : PrimaryCapacityVarianceRule {
    override fun check(item: PrimaryCapacityVariance): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacityVarianceCodeLengthRule(var maxLength: Int) : PrimaryCapacityVarianceRule {
    override fun check(item: PrimaryCapacityVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacityVarianceTitleLengthRule(var maxLength: Int) : PrimaryCapacityVarianceRule {
    override fun check(item: PrimaryCapacityVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacityVarianceSourceLengthRule(var maxLength: Int) : PrimaryCapacityVarianceRule {
    override fun check(item: PrimaryCapacityVariance): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacityVarianceSummaryLengthRule(var maxLength: Int) : PrimaryCapacityVarianceRule {
    override fun check(item: PrimaryCapacityVariance): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacityVarianceRequiredSourceRule(var message: String) : PrimaryCapacityVarianceRule {
    override fun check(item: PrimaryCapacityVariance): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary capacity variance. */
data class PrimaryCapacityVarianceValidator(var rules: MutableList<PrimaryCapacityVarianceRule>) {
    fun validate(item: PrimaryCapacityVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryCapacityVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCapacityVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCapacityVarianceValidator(): PrimaryCapacityVarianceValidator {
    return PrimaryCapacityVarianceValidator(
        mutableListOf<PrimaryCapacityVarianceRule>(
            PrimaryCapacityVarianceRequiredSummaryRule("primary capacity variance needs this field"),
            PrimaryCapacityVarianceCodeLengthRule(80),
            PrimaryCapacityVarianceTitleLengthRule(255),
            PrimaryCapacityVarianceSourceLengthRule(16),
            PrimaryCapacityVarianceSummaryLengthRule(120),
            PrimaryCapacityVarianceRequiredSourceRule("primary capacity variance needs a value"),
        ),
    )
}
