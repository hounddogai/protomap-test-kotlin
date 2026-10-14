package com.acme.transport.lastmile.proofofdelivery.score

/** A rule a tiered proof of delivery score must pass. Check returns its problems, or none. */
interface TieredProofOfDeliveryScoreRule {
    fun check(item: TieredProofOfDeliveryScore): MutableList<String>
}

data class TieredProofOfDeliveryScoreTimezoneLengthRule(var maxLength: Int) : TieredProofOfDeliveryScoreRule {
    override fun check(item: TieredProofOfDeliveryScore): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredProofOfDeliveryScoreRequiredTimezoneRule(
    var message: String,
) : TieredProofOfDeliveryScoreRule {
    override fun check(item: TieredProofOfDeliveryScore): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredProofOfDeliveryScoreCodeLengthRule(var maxLength: Int) : TieredProofOfDeliveryScoreRule {
    override fun check(item: TieredProofOfDeliveryScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredProofOfDeliveryScoreScoreRangeRule(
    var min: Double,
    var max: Double,
) : TieredProofOfDeliveryScoreRule {
    override fun check(item: TieredProofOfDeliveryScore): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered proof of delivery score. */
data class TieredProofOfDeliveryScoreValidator(var rules: MutableList<TieredProofOfDeliveryScoreRule>) {
    fun validate(item: TieredProofOfDeliveryScore): MutableList<String> {
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

    fun requireValid(item: TieredProofOfDeliveryScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredProofOfDeliveryScore>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredProofOfDeliveryScoreValidator(): TieredProofOfDeliveryScoreValidator {
    return TieredProofOfDeliveryScoreValidator(
        mutableListOf<TieredProofOfDeliveryScoreRule>(
            TieredProofOfDeliveryScoreTimezoneLengthRule(3600),
            TieredProofOfDeliveryScoreRequiredTimezoneRule("tiered proof of delivery score needs an entry"),
            TieredProofOfDeliveryScoreCodeLengthRule(16),
            TieredProofOfDeliveryScoreScoreRangeRule(5.1, 532.7),
        ),
    )
}
