package com.acme.transport.emissions.evcharge.regionalchargeslot

/** A rule a ev charge variance must pass. Check returns its problems, or none. */
interface EvChargeVarianceRule {
    fun check(item: EvChargeVariance): MutableList<String>
}

data class EvChargeVarianceRequiredNameRule(var message: String) : EvChargeVarianceRule {
    override fun check(item: EvChargeVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeVarianceQuantityRangeRule(var min: Int, var max: Int) : EvChargeVarianceRule {
    override fun check(item: EvChargeVariance): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class EvChargeVarianceChannelLengthRule(var maxLength: Int) : EvChargeVarianceRule {
    override fun check(item: EvChargeVariance): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge variance. */
data class EvChargeVarianceValidator(var rules: MutableList<EvChargeVarianceRule>) {
    fun validate(item: EvChargeVariance): MutableList<String> {
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

    fun requireValid(item: EvChargeVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeVarianceValidator(): EvChargeVarianceValidator {
    return EvChargeVarianceValidator(
        mutableListOf<EvChargeVarianceRule>(
            EvChargeVarianceRequiredNameRule("ev charge variance needs a value"),
            EvChargeVarianceQuantityRangeRule(64, 255),
            EvChargeVarianceChannelLengthRule(120),
        ),
    )
}
