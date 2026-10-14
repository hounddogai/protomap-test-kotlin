package com.acme.transport.lastmile.proofofdelivery.allocation

/** A rule a seasonal proof of delivery checkpoint must pass. Check returns its problems, or none. */
interface SeasonalProofOfDeliveryCheckpointRule {
    fun check(item: SeasonalProofOfDeliveryCheckpoint): MutableList<String>
}

data class SeasonalProofOfDeliveryCheckpointCodeLengthRule(
    var maxLength: Int,
) : SeasonalProofOfDeliveryCheckpointRule {
    override fun check(item: SeasonalProofOfDeliveryCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalProofOfDeliveryCheckpointRequiredLabelRule(
    var message: String,
) : SeasonalProofOfDeliveryCheckpointRule {
    override fun check(item: SeasonalProofOfDeliveryCheckpoint): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalProofOfDeliveryCheckpointRequiredPhoneRule(
    var message: String,
) : SeasonalProofOfDeliveryCheckpointRule {
    override fun check(item: SeasonalProofOfDeliveryCheckpoint): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalProofOfDeliveryCheckpointCodePrefixRule(
    var prefix: String,
) : SeasonalProofOfDeliveryCheckpointRule {
    override fun check(item: SeasonalProofOfDeliveryCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalProofOfDeliveryCheckpointLimitRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalProofOfDeliveryCheckpointRule {
    override fun check(item: SeasonalProofOfDeliveryCheckpoint): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class SeasonalProofOfDeliveryCheckpointPhoneLengthRule(
    var maxLength: Int,
) : SeasonalProofOfDeliveryCheckpointRule {
    override fun check(item: SeasonalProofOfDeliveryCheckpoint): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal proof of delivery checkpoint. */
data class SeasonalProofOfDeliveryCheckpointValidator(
    var rules: MutableList<SeasonalProofOfDeliveryCheckpointRule>,
) {
    fun validate(item: SeasonalProofOfDeliveryCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalProofOfDeliveryCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalProofOfDeliveryCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalProofOfDeliveryCheckpointValidator(): SeasonalProofOfDeliveryCheckpointValidator {
    return SeasonalProofOfDeliveryCheckpointValidator(
        mutableListOf<SeasonalProofOfDeliveryCheckpointRule>(
            SeasonalProofOfDeliveryCheckpointCodeLengthRule(32),
            SeasonalProofOfDeliveryCheckpointRequiredLabelRule(
                "seasonal proof of delivery checkpoint needs an entry",
            ),
            SeasonalProofOfDeliveryCheckpointRequiredPhoneRule(
                "seasonal proof of delivery checkpoint needs a value",
            ),
            SeasonalProofOfDeliveryCheckpointCodePrefixRule("REF"),
            SeasonalProofOfDeliveryCheckpointLimitRangeRule(32, 1000),
            SeasonalProofOfDeliveryCheckpointPhoneLengthRule(86400),
        ),
    )
}
