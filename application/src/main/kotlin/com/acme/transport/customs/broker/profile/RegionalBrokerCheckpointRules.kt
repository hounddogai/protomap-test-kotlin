package com.acme.transport.customs.broker.profile

/** A rule a regional broker checkpoint must pass. Check returns its problems, or none. */
interface RegionalBrokerCheckpointRule {
    fun check(item: RegionalBrokerCheckpoint): MutableList<String>
}

data class RegionalBrokerCheckpointRequiredUnitRule(var message: String) : RegionalBrokerCheckpointRule {
    override fun check(item: RegionalBrokerCheckpoint): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalBrokerCheckpointRateRangeRule(
    var min: Double,
    var max: Double,
) : RegionalBrokerCheckpointRule {
    override fun check(item: RegionalBrokerCheckpoint): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class RegionalBrokerCheckpointRequiredRegionRule(var message: String) : RegionalBrokerCheckpointRule {
    override fun check(item: RegionalBrokerCheckpoint): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalBrokerCheckpointUnitLengthRule(var maxLength: Int) : RegionalBrokerCheckpointRule {
    override fun check(item: RegionalBrokerCheckpoint): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalBrokerCheckpointRequiredCodeRule(var message: String) : RegionalBrokerCheckpointRule {
    override fun check(item: RegionalBrokerCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional broker checkpoint. */
data class RegionalBrokerCheckpointValidator(var rules: MutableList<RegionalBrokerCheckpointRule>) {
    fun validate(item: RegionalBrokerCheckpoint): MutableList<String> {
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

    fun requireValid(item: RegionalBrokerCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalBrokerCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalBrokerCheckpointValidator(): RegionalBrokerCheckpointValidator {
    return RegionalBrokerCheckpointValidator(
        mutableListOf<RegionalBrokerCheckpointRule>(
            RegionalBrokerCheckpointRequiredUnitRule("regional broker checkpoint needs an entry"),
            RegionalBrokerCheckpointRateRangeRule(4.9, 1932.9),
            RegionalBrokerCheckpointRequiredRegionRule("regional broker checkpoint needs an entry"),
            RegionalBrokerCheckpointUnitLengthRule(1000),
            RegionalBrokerCheckpointRequiredCodeRule("regional broker checkpoint needs an entry"),
        ),
    )
}
