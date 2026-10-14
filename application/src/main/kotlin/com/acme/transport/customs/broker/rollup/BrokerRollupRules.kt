package com.acme.transport.customs.broker.rollup

/** A rule a broker rollup must pass. Check returns its problems, or none. */
interface BrokerRollupRule {
    fun check(item: BrokerRollup): MutableList<String>
}

data class BrokerRollupThresholdRangeRule(var min: Double, var max: Double) : BrokerRollupRule {
    override fun check(item: BrokerRollup): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BrokerRollupDepotLengthRule(var maxLength: Int) : BrokerRollupRule {
    override fun check(item: BrokerRollup): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerRollupMarginRangeRule(var min: Double, var max: Double) : BrokerRollupRule {
    override fun check(item: BrokerRollup): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker rollup. */
data class BrokerRollupValidator(var rules: MutableList<BrokerRollupRule>) {
    fun validate(item: BrokerRollup): MutableList<String> {
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

    fun requireValid(item: BrokerRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerRollupValidator(): BrokerRollupValidator {
    return BrokerRollupValidator(
        mutableListOf<BrokerRollupRule>(
            BrokerRollupThresholdRangeRule(3.8, 2855.7),
            BrokerRollupDepotLengthRule(120),
            BrokerRollupMarginRangeRule(5.4, 4875.3),
        ),
    )
}
