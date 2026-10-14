package com.acme.transport.emissions.fuelburn.burnthreshold

/** A rule a adaptive fuel burn threshold must pass. Check returns its problems, or none. */
interface AdaptiveFuelBurnThresholdRule {
    fun check(item: AdaptiveFuelBurnThreshold): MutableList<String>
}

data class AdaptiveFuelBurnThresholdRequiredCodeRule(var message: String) : AdaptiveFuelBurnThresholdRule {
    override fun check(item: AdaptiveFuelBurnThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFuelBurnThresholdCodeLengthRule(var maxLength: Int) : AdaptiveFuelBurnThresholdRule {
    override fun check(item: AdaptiveFuelBurnThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFuelBurnThresholdSourceLengthRule(var maxLength: Int) : AdaptiveFuelBurnThresholdRule {
    override fun check(item: AdaptiveFuelBurnThreshold): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFuelBurnThresholdCodePrefixRule(var prefix: String) : AdaptiveFuelBurnThresholdRule {
    override fun check(item: AdaptiveFuelBurnThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFuelBurnThresholdRequiredLabelRule(var message: String) : AdaptiveFuelBurnThresholdRule {
    override fun check(item: AdaptiveFuelBurnThreshold): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive fuel burn threshold. */
data class AdaptiveFuelBurnThresholdValidator(var rules: MutableList<AdaptiveFuelBurnThresholdRule>) {
    fun validate(item: AdaptiveFuelBurnThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveFuelBurnThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveFuelBurnThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveFuelBurnThresholdValidator(): AdaptiveFuelBurnThresholdValidator {
    return AdaptiveFuelBurnThresholdValidator(
        mutableListOf<AdaptiveFuelBurnThresholdRule>(
            AdaptiveFuelBurnThresholdRequiredCodeRule("adaptive fuel burn threshold needs a value"),
            AdaptiveFuelBurnThresholdCodeLengthRule(3600),
            AdaptiveFuelBurnThresholdSourceLengthRule(64),
            AdaptiveFuelBurnThresholdCodePrefixRule("X"),
            AdaptiveFuelBurnThresholdRequiredLabelRule("adaptive fuel burn threshold needs an entry"),
        ),
    )
}
