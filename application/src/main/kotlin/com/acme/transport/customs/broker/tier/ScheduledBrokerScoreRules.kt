package com.acme.transport.customs.broker.tier

/** A rule a scheduled broker score must pass. Check returns its problems, or none. */
interface ScheduledBrokerScoreRule {
    fun check(item: ScheduledBrokerScore): MutableList<String>
}

data class ScheduledBrokerScoreRatioRangeRule(var min: Double, var max: Double) : ScheduledBrokerScoreRule {
    override fun check(item: ScheduledBrokerScore): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBrokerScoreCapacityRangeRule(var min: Int, var max: Int) : ScheduledBrokerScoreRule {
    override fun check(item: ScheduledBrokerScore): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBrokerScoreRequiredLabelRule(var message: String) : ScheduledBrokerScoreRule {
    override fun check(item: ScheduledBrokerScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBrokerScoreLabelLengthRule(var maxLength: Int) : ScheduledBrokerScoreRule {
    override fun check(item: ScheduledBrokerScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBrokerScoreRequiredCodeRule(var message: String) : ScheduledBrokerScoreRule {
    override fun check(item: ScheduledBrokerScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBrokerScoreShiftCodeLengthRule(var maxLength: Int) : ScheduledBrokerScoreRule {
    override fun check(item: ScheduledBrokerScore): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled broker score. */
data class ScheduledBrokerScoreValidator(var rules: MutableList<ScheduledBrokerScoreRule>) {
    fun validate(item: ScheduledBrokerScore): MutableList<String> {
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

    fun requireValid(item: ScheduledBrokerScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledBrokerScore>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledBrokerScoreValidator(): ScheduledBrokerScoreValidator {
    return ScheduledBrokerScoreValidator(
        mutableListOf<ScheduledBrokerScoreRule>(
            ScheduledBrokerScoreRatioRangeRule(5.2, 3641.6),
            ScheduledBrokerScoreCapacityRangeRule(16, 120),
            ScheduledBrokerScoreRequiredLabelRule("scheduled broker score needs an entry"),
            ScheduledBrokerScoreLabelLengthRule(255),
            ScheduledBrokerScoreRequiredCodeRule("scheduled broker score needs a value"),
            ScheduledBrokerScoreShiftCodeLengthRule(1000),
        ),
    )
}
