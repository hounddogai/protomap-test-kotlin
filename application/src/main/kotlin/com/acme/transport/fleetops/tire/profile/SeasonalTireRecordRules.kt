package com.acme.transport.fleetops.tire.profile

/** A rule a seasonal tire record must pass. Check returns its problems, or none. */
interface SeasonalTireRecordRule {
    fun check(item: SeasonalTireRecord): MutableList<String>
}

data class SeasonalTireRecordRequiredDockRule(var message: String) : SeasonalTireRecordRule {
    override fun check(item: SeasonalTireRecord): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTireRecordDiscountRangeRule(var min: Double, var max: Double) : SeasonalTireRecordRule {
    override fun check(item: SeasonalTireRecord): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTireRecordBatchSizeRangeRule(var min: Int, var max: Int) : SeasonalTireRecordRule {
    override fun check(item: SeasonalTireRecord): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTireRecordRequiredRouteCodeRule(var message: String) : SeasonalTireRecordRule {
    override fun check(item: SeasonalTireRecord): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTireRecordLabelLengthRule(var maxLength: Int) : SeasonalTireRecordRule {
    override fun check(item: SeasonalTireRecord): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTireRecordRequiredLabelRule(var message: String) : SeasonalTireRecordRule {
    override fun check(item: SeasonalTireRecord): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal tire record. */
data class SeasonalTireRecordValidator(var rules: MutableList<SeasonalTireRecordRule>) {
    fun validate(item: SeasonalTireRecord): MutableList<String> {
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

    fun requireValid(item: SeasonalTireRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTireRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTireRecordValidator(): SeasonalTireRecordValidator {
    return SeasonalTireRecordValidator(
        mutableListOf<SeasonalTireRecordRule>(
            SeasonalTireRecordRequiredDockRule("seasonal tire record needs this field"),
            SeasonalTireRecordDiscountRangeRule(8.4, 4975.9),
            SeasonalTireRecordBatchSizeRangeRule(64, 1000),
            SeasonalTireRecordRequiredRouteCodeRule("seasonal tire record needs this field"),
            SeasonalTireRecordLabelLengthRule(3600),
            SeasonalTireRecordRequiredLabelRule("seasonal tire record needs this field"),
        ),
    )
}
