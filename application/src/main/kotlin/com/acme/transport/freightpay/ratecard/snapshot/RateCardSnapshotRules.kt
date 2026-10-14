package com.acme.transport.freightpay.ratecard.snapshot

/** A rule a rate card snapshot must pass. Check returns its problems, or none. */
interface RateCardSnapshotRule {
    fun check(item: RateCardSnapshot): MutableList<String>
}

data class RateCardSnapshotAmountRangeRule(var min: Double, var max: Double) : RateCardSnapshotRule {
    override fun check(item: RateCardSnapshot): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RateCardSnapshotFloorValueRangeRule(var min: Double, var max: Double) : RateCardSnapshotRule {
    override fun check(item: RateCardSnapshot): MutableList<String> {
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

data class RateCardSnapshotLabelLengthRule(var maxLength: Int) : RateCardSnapshotRule {
    override fun check(item: RateCardSnapshot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardSnapshotCodePrefixRule(var prefix: String) : RateCardSnapshotRule {
    override fun check(item: RateCardSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RateCardSnapshotSummaryLengthRule(var maxLength: Int) : RateCardSnapshotRule {
    override fun check(item: RateCardSnapshot): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardSnapshotRequiredSummaryRule(var message: String) : RateCardSnapshotRule {
    override fun check(item: RateCardSnapshot): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card snapshot. */
data class RateCardSnapshotValidator(var rules: MutableList<RateCardSnapshotRule>) {
    fun validate(item: RateCardSnapshot): MutableList<String> {
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

    fun requireValid(item: RateCardSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardSnapshotValidator(): RateCardSnapshotValidator {
    return RateCardSnapshotValidator(
        mutableListOf<RateCardSnapshotRule>(
            RateCardSnapshotAmountRangeRule(4.3, 985.5),
            RateCardSnapshotFloorValueRangeRule(8.0, 2906.6),
            RateCardSnapshotLabelLengthRule(80),
            RateCardSnapshotCodePrefixRule("REF"),
            RateCardSnapshotSummaryLengthRule(80),
            RateCardSnapshotRequiredSummaryRule("rate card snapshot needs this field"),
        ),
    )
}
