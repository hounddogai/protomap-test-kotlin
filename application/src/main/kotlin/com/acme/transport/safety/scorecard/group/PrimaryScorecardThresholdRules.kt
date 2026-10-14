package com.acme.transport.safety.scorecard.group

/** A rule a primary scorecard threshold must pass. Check returns its problems, or none. */
interface PrimaryScorecardThresholdRule {
    fun check(item: PrimaryScorecardThreshold): MutableList<String>
}

data class PrimaryScorecardThresholdCurrencyLengthRule(var maxLength: Int) : PrimaryScorecardThresholdRule {
    override fun check(item: PrimaryScorecardThreshold): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardThresholdRequiredExternalRefRule(
    var message: String,
) : PrimaryScorecardThresholdRule {
    override fun check(item: PrimaryScorecardThreshold): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardThresholdRequiredCodeRule(var message: String) : PrimaryScorecardThresholdRule {
    override fun check(item: PrimaryScorecardThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardThresholdExternalRefLengthRule(var maxLength: Int) : PrimaryScorecardThresholdRule {
    override fun check(item: PrimaryScorecardThreshold): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardThresholdRequiredCurrencyRule(var message: String) : PrimaryScorecardThresholdRule {
    override fun check(item: PrimaryScorecardThreshold): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardThresholdTitleLengthRule(var maxLength: Int) : PrimaryScorecardThresholdRule {
    override fun check(item: PrimaryScorecardThreshold): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary scorecard threshold. */
data class PrimaryScorecardThresholdValidator(var rules: MutableList<PrimaryScorecardThresholdRule>) {
    fun validate(item: PrimaryScorecardThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryScorecardThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryScorecardThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryScorecardThresholdValidator(): PrimaryScorecardThresholdValidator {
    return PrimaryScorecardThresholdValidator(
        mutableListOf<PrimaryScorecardThresholdRule>(
            PrimaryScorecardThresholdCurrencyLengthRule(255),
            PrimaryScorecardThresholdRequiredExternalRefRule("primary scorecard threshold needs this field"),
            PrimaryScorecardThresholdRequiredCodeRule("primary scorecard threshold needs a value"),
            PrimaryScorecardThresholdExternalRefLengthRule(32),
            PrimaryScorecardThresholdRequiredCurrencyRule("primary scorecard threshold needs this field"),
            PrimaryScorecardThresholdTitleLengthRule(86400),
        ),
    )
}
