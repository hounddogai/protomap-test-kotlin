package com.acme.transport.network.transittime.request

/** A rule a transit time score must pass. Check returns its problems, or none. */
interface TransitTimeScoreRule {
    fun check(item: TransitTimeScore): MutableList<String>
}

data class TransitTimeScoreRequiredCodeRule(var message: String) : TransitTimeScoreRule {
    override fun check(item: TransitTimeScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeScoreRequiredTitleRule(var message: String) : TransitTimeScoreRule {
    override fun check(item: TransitTimeScore): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeScoreCapacityRangeRule(var min: Int, var max: Int) : TransitTimeScoreRule {
    override fun check(item: TransitTimeScore): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeScoreChannelLengthRule(var maxLength: Int) : TransitTimeScoreRule {
    override fun check(item: TransitTimeScore): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeScoreCodePrefixRule(var prefix: String) : TransitTimeScoreRule {
    override fun check(item: TransitTimeScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeScoreRatioRangeRule(var min: Double, var max: Double) : TransitTimeScoreRule {
    override fun check(item: TransitTimeScore): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time score. */
data class TransitTimeScoreValidator(var rules: MutableList<TransitTimeScoreRule>) {
    fun validate(item: TransitTimeScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeScore>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeScoreValidator(): TransitTimeScoreValidator {
    return TransitTimeScoreValidator(
        mutableListOf<TransitTimeScoreRule>(
            TransitTimeScoreRequiredCodeRule("transit time score needs this field"),
            TransitTimeScoreRequiredTitleRule("transit time score needs an entry"),
            TransitTimeScoreCapacityRangeRule(255, 1000),
            TransitTimeScoreChannelLengthRule(64),
            TransitTimeScoreCodePrefixRule("REF"),
            TransitTimeScoreRatioRangeRule(4.9, 2014.7),
        ),
    )
}
