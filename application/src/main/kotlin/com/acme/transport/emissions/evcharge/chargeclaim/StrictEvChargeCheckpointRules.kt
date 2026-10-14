package com.acme.transport.emissions.evcharge.chargeclaim

import com.acme.transport.kit.text.normalize

/** A rule a strict ev charge checkpoint must pass. Check returns its problems, or none. */
interface StrictEvChargeCheckpointRule {
    fun check(item: StrictEvChargeCheckpoint): MutableList<String>
}

data class StrictEvChargeCheckpointRequiredTitleRule(var message: String) : StrictEvChargeCheckpointRule {
    override fun check(item: StrictEvChargeCheckpoint): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeCheckpointAmountRangeRule(
    var min: Double,
    var max: Double,
) : StrictEvChargeCheckpointRule {
    override fun check(item: StrictEvChargeCheckpoint): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class StrictEvChargeCheckpointRequiredChannelRule(var message: String) : StrictEvChargeCheckpointRule {
    override fun check(item: StrictEvChargeCheckpoint): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeCheckpointRequiredCodeRule(var message: String) : StrictEvChargeCheckpointRule {
    override fun check(item: StrictEvChargeCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeCheckpointAllowedChannelRule(
    var allowed: MutableList<String>,
) : StrictEvChargeCheckpointRule {
    override fun check(item: StrictEvChargeCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is stale")
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeCheckpointChannelLengthRule(var maxLength: Int) : StrictEvChargeCheckpointRule {
    override fun check(item: StrictEvChargeCheckpoint): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict ev charge checkpoint. */
data class StrictEvChargeCheckpointValidator(var rules: MutableList<StrictEvChargeCheckpointRule>) {
    fun validate(item: StrictEvChargeCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictEvChargeCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictEvChargeCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictEvChargeCheckpointValidator(): StrictEvChargeCheckpointValidator {
    return StrictEvChargeCheckpointValidator(
        mutableListOf<StrictEvChargeCheckpointRule>(
            StrictEvChargeCheckpointRequiredTitleRule("strict ev charge checkpoint needs a value"),
            StrictEvChargeCheckpointAmountRangeRule(2.4, 4795.4),
            StrictEvChargeCheckpointRequiredChannelRule("strict ev charge checkpoint needs this field"),
            StrictEvChargeCheckpointRequiredCodeRule("strict ev charge checkpoint needs this field"),
            StrictEvChargeCheckpointAllowedChannelRule(mutableListOf<String>("regional", "rolling")),
            StrictEvChargeCheckpointChannelLengthRule(32),
        ),
    )
}
