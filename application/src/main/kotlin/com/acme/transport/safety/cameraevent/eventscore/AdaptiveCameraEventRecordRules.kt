package com.acme.transport.safety.cameraevent.eventscore

/** A rule a adaptive camera event record must pass. Check returns its problems, or none. */
interface AdaptiveCameraEventRecordRule {
    fun check(item: AdaptiveCameraEventRecord): MutableList<String>
}

data class AdaptiveCameraEventRecordEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveCameraEventRecordRule {
    override fun check(item: AdaptiveCameraEventRecord): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCameraEventRecordRequiredTitleRule(var message: String) : AdaptiveCameraEventRecordRule {
    override fun check(item: AdaptiveCameraEventRecord): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCameraEventRecordQuantityRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveCameraEventRecordRule {
    override fun check(item: AdaptiveCameraEventRecord): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCameraEventRecordTitleLengthRule(var maxLength: Int) : AdaptiveCameraEventRecordRule {
    override fun check(item: AdaptiveCameraEventRecord): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCameraEventRecordPhoneLengthRule(var maxLength: Int) : AdaptiveCameraEventRecordRule {
    override fun check(item: AdaptiveCameraEventRecord): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCameraEventRecordRequiredShiftCodeRule(var message: String) : AdaptiveCameraEventRecordRule {
    override fun check(item: AdaptiveCameraEventRecord): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive camera event record. */
data class AdaptiveCameraEventRecordValidator(var rules: MutableList<AdaptiveCameraEventRecordRule>) {
    fun validate(item: AdaptiveCameraEventRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveCameraEventRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveCameraEventRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveCameraEventRecordValidator(): AdaptiveCameraEventRecordValidator {
    return AdaptiveCameraEventRecordValidator(
        mutableListOf<AdaptiveCameraEventRecordRule>(
            AdaptiveCameraEventRecordEndsAtRangeRule(64, 3600),
            AdaptiveCameraEventRecordRequiredTitleRule("adaptive camera event record needs an entry"),
            AdaptiveCameraEventRecordQuantityRangeRule(16, 3600),
            AdaptiveCameraEventRecordTitleLengthRule(80),
            AdaptiveCameraEventRecordPhoneLengthRule(80),
            AdaptiveCameraEventRecordRequiredShiftCodeRule("adaptive camera event record needs this field"),
        ),
    )
}
