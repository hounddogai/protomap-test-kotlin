package com.acme.transport.emissions.emissionfactor.claim

/** A rule a tiered emission factor threshold must pass. Check returns its problems, or none. */
interface TieredEmissionFactorThresholdRule {
    fun check(item: TieredEmissionFactorThreshold): MutableList<String>
}

data class TieredEmissionFactorThresholdCodeLengthRule(var maxLength: Int) : TieredEmissionFactorThresholdRule {
    override fun check(item: TieredEmissionFactorThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredEmissionFactorThresholdTitleLengthRule(
    var maxLength: Int,
) : TieredEmissionFactorThresholdRule {
    override fun check(item: TieredEmissionFactorThreshold): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredEmissionFactorThresholdRequiredRegionRule(
    var message: String,
) : TieredEmissionFactorThresholdRule {
    override fun check(item: TieredEmissionFactorThreshold): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered emission factor threshold. */
data class TieredEmissionFactorThresholdValidator(var rules: MutableList<TieredEmissionFactorThresholdRule>) {
    fun validate(item: TieredEmissionFactorThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredEmissionFactorThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredEmissionFactorThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredEmissionFactorThresholdValidator(): TieredEmissionFactorThresholdValidator {
    return TieredEmissionFactorThresholdValidator(
        mutableListOf<TieredEmissionFactorThresholdRule>(
            TieredEmissionFactorThresholdCodeLengthRule(1000),
            TieredEmissionFactorThresholdTitleLengthRule(3600),
            TieredEmissionFactorThresholdRequiredRegionRule("tiered emission factor threshold needs an entry"),
        ),
    )
}
