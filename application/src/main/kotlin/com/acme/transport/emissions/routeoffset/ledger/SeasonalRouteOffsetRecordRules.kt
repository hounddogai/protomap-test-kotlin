package com.acme.transport.emissions.routeoffset.ledger

/** A rule a seasonal route offset record must pass. Check returns its problems, or none. */
interface SeasonalRouteOffsetRecordRule {
    fun check(item: SeasonalRouteOffsetRecord): MutableList<String>
}

data class SeasonalRouteOffsetRecordPalletsRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalRouteOffsetRecordRule {
    override fun check(item: SeasonalRouteOffsetRecord): MutableList<String> {
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

data class SeasonalRouteOffsetRecordNameLengthRule(var maxLength: Int) : SeasonalRouteOffsetRecordRule {
    override fun check(item: SeasonalRouteOffsetRecord): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRouteOffsetRecordWeightRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalRouteOffsetRecordRule {
    override fun check(item: SeasonalRouteOffsetRecord): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class SeasonalRouteOffsetRecordRequiredNameRule(var message: String) : SeasonalRouteOffsetRecordRule {
    override fun check(item: SeasonalRouteOffsetRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRouteOffsetRecordCodeLengthRule(var maxLength: Int) : SeasonalRouteOffsetRecordRule {
    override fun check(item: SeasonalRouteOffsetRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal route offset record. */
data class SeasonalRouteOffsetRecordValidator(var rules: MutableList<SeasonalRouteOffsetRecordRule>) {
    fun validate(item: SeasonalRouteOffsetRecord): MutableList<String> {
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

    fun requireValid(item: SeasonalRouteOffsetRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRouteOffsetRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRouteOffsetRecordValidator(): SeasonalRouteOffsetRecordValidator {
    return SeasonalRouteOffsetRecordValidator(
        mutableListOf<SeasonalRouteOffsetRecordRule>(
            SeasonalRouteOffsetRecordPalletsRangeRule(64, 86400),
            SeasonalRouteOffsetRecordNameLengthRule(3600),
            SeasonalRouteOffsetRecordWeightRangeRule(8.0, 1577.6),
            SeasonalRouteOffsetRecordRequiredNameRule("seasonal route offset record needs an entry"),
            SeasonalRouteOffsetRecordCodeLengthRule(16),
        ),
    )
}
