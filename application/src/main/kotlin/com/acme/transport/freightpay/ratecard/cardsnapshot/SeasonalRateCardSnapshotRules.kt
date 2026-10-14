package com.acme.transport.freightpay.ratecard.cardsnapshot

/** A rule a seasonal rate card snapshot must pass. Check returns its problems, or none. */
interface SeasonalRateCardSnapshotRule {
    fun check(item: SeasonalRateCardSnapshot): MutableList<String>
}

data class SeasonalRateCardSnapshotRequiredNameRule(var message: String) : SeasonalRateCardSnapshotRule {
    override fun check(item: SeasonalRateCardSnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRateCardSnapshotCodePrefixRule(var prefix: String) : SeasonalRateCardSnapshotRule {
    override fun check(item: SeasonalRateCardSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRateCardSnapshotCodeLengthRule(var maxLength: Int) : SeasonalRateCardSnapshotRule {
    override fun check(item: SeasonalRateCardSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRateCardSnapshotNameLengthRule(var maxLength: Int) : SeasonalRateCardSnapshotRule {
    override fun check(item: SeasonalRateCardSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRateCardSnapshotFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalRateCardSnapshotRule {
    override fun check(item: SeasonalRateCardSnapshot): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRateCardSnapshotActiveGuardRule(var limit: Double) : SeasonalRateCardSnapshotRule {
    override fun check(item: SeasonalRateCardSnapshot): MutableList<String> {
        if (item.active && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal rate card snapshot. */
data class SeasonalRateCardSnapshotValidator(var rules: MutableList<SeasonalRateCardSnapshotRule>) {
    fun validate(item: SeasonalRateCardSnapshot): MutableList<String> {
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

    fun requireValid(item: SeasonalRateCardSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRateCardSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRateCardSnapshotValidator(): SeasonalRateCardSnapshotValidator {
    return SeasonalRateCardSnapshotValidator(
        mutableListOf<SeasonalRateCardSnapshotRule>(
            SeasonalRateCardSnapshotRequiredNameRule("seasonal rate card snapshot needs a value"),
            SeasonalRateCardSnapshotCodePrefixRule("REF"),
            SeasonalRateCardSnapshotCodeLengthRule(255),
            SeasonalRateCardSnapshotNameLengthRule(32),
            SeasonalRateCardSnapshotFloorValueRangeRule(0.7, 1835.4),
            SeasonalRateCardSnapshotActiveGuardRule(9.3),
        ),
    )
}
