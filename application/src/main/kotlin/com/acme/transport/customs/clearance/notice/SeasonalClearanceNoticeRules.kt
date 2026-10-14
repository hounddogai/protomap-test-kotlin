package com.acme.transport.customs.clearance.notice

/** A rule a seasonal clearance notice must pass. Check returns its problems, or none. */
interface SeasonalClearanceNoticeRule {
    fun check(item: SeasonalClearanceNotice): MutableList<String>
}

data class SeasonalClearanceNoticeCodeLengthRule(var maxLength: Int) : SeasonalClearanceNoticeRule {
    override fun check(item: SeasonalClearanceNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalClearanceNoticeVehicleIdLengthRule(var maxLength: Int) : SeasonalClearanceNoticeRule {
    override fun check(item: SeasonalClearanceNotice): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalClearanceNoticeBatchSizeRangeRule(var min: Int, var max: Int) : SeasonalClearanceNoticeRule {
    override fun check(item: SeasonalClearanceNotice): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalClearanceNoticeCodePrefixRule(var prefix: String) : SeasonalClearanceNoticeRule {
    override fun check(item: SeasonalClearanceNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal clearance notice. */
data class SeasonalClearanceNoticeValidator(var rules: MutableList<SeasonalClearanceNoticeRule>) {
    fun validate(item: SeasonalClearanceNotice): MutableList<String> {
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

    fun requireValid(item: SeasonalClearanceNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalClearanceNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalClearanceNoticeValidator(): SeasonalClearanceNoticeValidator {
    return SeasonalClearanceNoticeValidator(
        mutableListOf<SeasonalClearanceNoticeRule>(
            SeasonalClearanceNoticeCodeLengthRule(80),
            SeasonalClearanceNoticeVehicleIdLengthRule(16),
            SeasonalClearanceNoticeBatchSizeRangeRule(80, 86400),
            SeasonalClearanceNoticeCodePrefixRule("OPS"),
        ),
    )
}
