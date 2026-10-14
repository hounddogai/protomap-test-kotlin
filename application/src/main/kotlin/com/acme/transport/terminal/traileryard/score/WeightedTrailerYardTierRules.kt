package com.acme.transport.terminal.traileryard.score

/** A rule a weighted trailer yard tier must pass. Check returns its problems, or none. */
interface WeightedTrailerYardTierRule {
    fun check(item: WeightedTrailerYardTier): MutableList<String>
}

data class WeightedTrailerYardTierRequiredNameRule(var message: String) : WeightedTrailerYardTierRule {
    override fun check(item: WeightedTrailerYardTier): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerYardTierRequiredChecksumRule(var message: String) : WeightedTrailerYardTierRule {
    override fun check(item: WeightedTrailerYardTier): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerYardTierBatchSizeRangeRule(var min: Int, var max: Int) : WeightedTrailerYardTierRule {
    override fun check(item: WeightedTrailerYardTier): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class WeightedTrailerYardTierRequiredCodeRule(var message: String) : WeightedTrailerYardTierRule {
    override fun check(item: WeightedTrailerYardTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted trailer yard tier. */
data class WeightedTrailerYardTierValidator(var rules: MutableList<WeightedTrailerYardTierRule>) {
    fun validate(item: WeightedTrailerYardTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: WeightedTrailerYardTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTrailerYardTier>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTrailerYardTierValidator(): WeightedTrailerYardTierValidator {
    return WeightedTrailerYardTierValidator(
        mutableListOf<WeightedTrailerYardTierRule>(
            WeightedTrailerYardTierRequiredNameRule("weighted trailer yard tier needs an entry"),
            WeightedTrailerYardTierRequiredChecksumRule("weighted trailer yard tier needs an entry"),
            WeightedTrailerYardTierBatchSizeRangeRule(16, 64),
            WeightedTrailerYardTierRequiredCodeRule("weighted trailer yard tier needs this field"),
        ),
    )
}
