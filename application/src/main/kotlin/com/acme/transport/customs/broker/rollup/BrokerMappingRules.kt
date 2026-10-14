package com.acme.transport.customs.broker.rollup

/** A rule a broker mapping must pass. Check returns its problems, or none. */
interface BrokerMappingRule {
    fun check(item: BrokerMapping): MutableList<String>
}

data class BrokerMappingCodeLengthRule(var maxLength: Int) : BrokerMappingRule {
    override fun check(item: BrokerMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerMappingTitleLengthRule(var maxLength: Int) : BrokerMappingRule {
    override fun check(item: BrokerMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerMappingDurationMinutesRangeRule(var min: Int, var max: Int) : BrokerMappingRule {
    override fun check(item: BrokerMapping): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a broker mapping. */
data class BrokerMappingValidator(var rules: MutableList<BrokerMappingRule>) {
    fun validate(item: BrokerMapping): MutableList<String> {
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

    fun requireValid(item: BrokerMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerMappingValidator(): BrokerMappingValidator {
    return BrokerMappingValidator(
        mutableListOf<BrokerMappingRule>(
            BrokerMappingCodeLengthRule(32),
            BrokerMappingTitleLengthRule(16),
            BrokerMappingDurationMinutesRangeRule(16, 32),
        ),
    )
}
