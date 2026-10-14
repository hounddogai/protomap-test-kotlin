package com.acme.transport.safety.cameraevent.defaulteventscore

/** A rule a tiered camera event score must pass. Check returns its problems, or none. */
interface TieredCameraEventScoreRule {
    fun check(item: TieredCameraEventScore): MutableList<String>
}

data class TieredCameraEventScoreRequiredCodeRule(var message: String) : TieredCameraEventScoreRule {
    override fun check(item: TieredCameraEventScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCameraEventScoreRequiredLaneRule(var message: String) : TieredCameraEventScoreRule {
    override fun check(item: TieredCameraEventScore): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCameraEventScoreFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : TieredCameraEventScoreRule {
    override fun check(item: TieredCameraEventScore): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class TieredCameraEventScoreTitleLengthRule(var maxLength: Int) : TieredCameraEventScoreRule {
    override fun check(item: TieredCameraEventScore): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCameraEventScoreCodePrefixRule(var prefix: String) : TieredCameraEventScoreRule {
    override fun check(item: TieredCameraEventScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered camera event score. */
data class TieredCameraEventScoreValidator(var rules: MutableList<TieredCameraEventScoreRule>) {
    fun validate(item: TieredCameraEventScore): MutableList<String> {
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

    fun requireValid(item: TieredCameraEventScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCameraEventScore>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCameraEventScoreValidator(): TieredCameraEventScoreValidator {
    return TieredCameraEventScoreValidator(
        mutableListOf<TieredCameraEventScoreRule>(
            TieredCameraEventScoreRequiredCodeRule("tiered camera event score needs an entry"),
            TieredCameraEventScoreRequiredLaneRule("tiered camera event score needs a value"),
            TieredCameraEventScoreFloorValueRangeRule(4.6, 4025.2),
            TieredCameraEventScoreTitleLengthRule(16),
            TieredCameraEventScoreCodePrefixRule("X"),
        ),
    )
}
