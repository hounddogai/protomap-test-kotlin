package com.acme.transport.safety.cameraevent.eventscore

/** A rule a default camera event score must pass. Check returns its problems, or none. */
interface DefaultCameraEventScoreRule {
    fun check(item: DefaultCameraEventScore): MutableList<String>
}

data class DefaultCameraEventScoreRateRangeRule(
    var min: Double,
    var max: Double,
) : DefaultCameraEventScoreRule {
    override fun check(item: DefaultCameraEventScore): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCameraEventScoreStopCountRangeRule(var min: Int, var max: Int) : DefaultCameraEventScoreRule {
    override fun check(item: DefaultCameraEventScore): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class DefaultCameraEventScorePriorityRangeRule(var min: Int, var max: Int) : DefaultCameraEventScoreRule {
    override fun check(item: DefaultCameraEventScore): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class DefaultCameraEventScoreRouteCodeLengthRule(var maxLength: Int) : DefaultCameraEventScoreRule {
    override fun check(item: DefaultCameraEventScore): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCameraEventScoreRequiredLabelRule(var message: String) : DefaultCameraEventScoreRule {
    override fun check(item: DefaultCameraEventScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCameraEventScoreCodeLengthRule(var maxLength: Int) : DefaultCameraEventScoreRule {
    override fun check(item: DefaultCameraEventScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default camera event score. */
data class DefaultCameraEventScoreValidator(var rules: MutableList<DefaultCameraEventScoreRule>) {
    fun validate(item: DefaultCameraEventScore): MutableList<String> {
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

    fun requireValid(item: DefaultCameraEventScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCameraEventScore>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCameraEventScoreValidator(): DefaultCameraEventScoreValidator {
    return DefaultCameraEventScoreValidator(
        mutableListOf<DefaultCameraEventScoreRule>(
            DefaultCameraEventScoreRateRangeRule(4.1, 4315.5),
            DefaultCameraEventScoreStopCountRangeRule(80, 120),
            DefaultCameraEventScorePriorityRangeRule(255, 86400),
            DefaultCameraEventScoreRouteCodeLengthRule(255),
            DefaultCameraEventScoreRequiredLabelRule("default camera event score needs this field"),
            DefaultCameraEventScoreCodeLengthRule(255),
        ),
    )
}
