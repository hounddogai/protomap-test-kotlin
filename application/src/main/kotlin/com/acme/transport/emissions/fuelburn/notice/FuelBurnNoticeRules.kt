package com.acme.transport.emissions.fuelburn.notice

/** A rule a fuel burn notice must pass. Check returns its problems, or none. */
interface FuelBurnNoticeRule {
    fun check(item: FuelBurnNotice): MutableList<String>
}

data class FuelBurnNoticeStopCountRangeRule(var min: Int, var max: Int) : FuelBurnNoticeRule {
    override fun check(item: FuelBurnNotice): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnNoticeCodeLengthRule(var maxLength: Int) : FuelBurnNoticeRule {
    override fun check(item: FuelBurnNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnNoticeLabelLengthRule(var maxLength: Int) : FuelBurnNoticeRule {
    override fun check(item: FuelBurnNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnNoticeLocaleLengthRule(var maxLength: Int) : FuelBurnNoticeRule {
    override fun check(item: FuelBurnNotice): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn notice. */
data class FuelBurnNoticeValidator(var rules: MutableList<FuelBurnNoticeRule>) {
    fun validate(item: FuelBurnNotice): MutableList<String> {
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

    fun requireValid(item: FuelBurnNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnNoticeValidator(): FuelBurnNoticeValidator {
    return FuelBurnNoticeValidator(
        mutableListOf<FuelBurnNoticeRule>(
            FuelBurnNoticeStopCountRangeRule(120, 86400),
            FuelBurnNoticeCodeLengthRule(64),
            FuelBurnNoticeLabelLengthRule(1000),
            FuelBurnNoticeLocaleLengthRule(16),
        ),
    )
}
