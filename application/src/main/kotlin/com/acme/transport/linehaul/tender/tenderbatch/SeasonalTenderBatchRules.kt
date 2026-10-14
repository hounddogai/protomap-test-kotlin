package com.acme.transport.linehaul.tender.tenderbatch

/** A rule a seasonal tender batch must pass. Check returns its problems, or none. */
interface SeasonalTenderBatchRule {
    fun check(item: SeasonalTenderBatch): MutableList<String>
}

data class SeasonalTenderBatchStopCountRangeRule(var min: Int, var max: Int) : SeasonalTenderBatchRule {
    override fun check(item: SeasonalTenderBatch): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTenderBatchRequiredShiftCodeRule(var message: String) : SeasonalTenderBatchRule {
    override fun check(item: SeasonalTenderBatch): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTenderBatchPhoneLengthRule(var maxLength: Int) : SeasonalTenderBatchRule {
    override fun check(item: SeasonalTenderBatch): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal tender batch. */
data class SeasonalTenderBatchValidator(var rules: MutableList<SeasonalTenderBatchRule>) {
    fun validate(item: SeasonalTenderBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalTenderBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTenderBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTenderBatchValidator(): SeasonalTenderBatchValidator {
    return SeasonalTenderBatchValidator(
        mutableListOf<SeasonalTenderBatchRule>(
            SeasonalTenderBatchStopCountRangeRule(80, 3600),
            SeasonalTenderBatchRequiredShiftCodeRule("seasonal tender batch needs an entry"),
            SeasonalTenderBatchPhoneLengthRule(32),
        ),
    )
}
