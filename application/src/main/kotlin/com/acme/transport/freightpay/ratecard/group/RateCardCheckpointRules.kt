package com.acme.transport.freightpay.ratecard.group

/** A rule a rate card checkpoint must pass. Check returns its problems, or none. */
interface RateCardCheckpointRule {
    fun check(item: RateCardCheckpoint): MutableList<String>
}

data class RateCardCheckpointPalletsRangeRule(var min: Int, var max: Int) : RateCardCheckpointRule {
    override fun check(item: RateCardCheckpoint): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class RateCardCheckpointRequiredCodeRule(var message: String) : RateCardCheckpointRule {
    override fun check(item: RateCardCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardCheckpointCodePrefixRule(var prefix: String) : RateCardCheckpointRule {
    override fun check(item: RateCardCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RateCardCheckpointCreatedAtRangeRule(var min: Int, var max: Int) : RateCardCheckpointRule {
    override fun check(item: RateCardCheckpoint): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rate card checkpoint. */
data class RateCardCheckpointValidator(var rules: MutableList<RateCardCheckpointRule>) {
    fun validate(item: RateCardCheckpoint): MutableList<String> {
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

    fun requireValid(item: RateCardCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardCheckpointValidator(): RateCardCheckpointValidator {
    return RateCardCheckpointValidator(
        mutableListOf<RateCardCheckpointRule>(
            RateCardCheckpointPalletsRangeRule(64, 64),
            RateCardCheckpointRequiredCodeRule("rate card checkpoint needs an entry"),
            RateCardCheckpointCodePrefixRule("REF"),
            RateCardCheckpointCreatedAtRangeRule(32, 120),
        ),
    )
}
