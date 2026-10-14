package com.acme.transport.freightpay.dispute.manifest

/** A rule a lenient dispute tier must pass. Check returns its problems, or none. */
interface LenientDisputeTierRule {
    fun check(item: LenientDisputeTier): MutableList<String>
}

data class LenientDisputeTierRequiredCodeRule(var message: String) : LenientDisputeTierRule {
    override fun check(item: LenientDisputeTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeTierDriverIdLengthRule(var maxLength: Int) : LenientDisputeTierRule {
    override fun check(item: LenientDisputeTier): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeTierRequiredDriverIdRule(var message: String) : LenientDisputeTierRule {
    override fun check(item: LenientDisputeTier): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeTierRequiredTitleRule(var message: String) : LenientDisputeTierRule {
    override fun check(item: LenientDisputeTier): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeTierAmountRangeRule(var min: Double, var max: Double) : LenientDisputeTierRule {
    override fun check(item: LenientDisputeTier): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient dispute tier. */
data class LenientDisputeTierValidator(var rules: MutableList<LenientDisputeTierRule>) {
    fun validate(item: LenientDisputeTier): MutableList<String> {
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

    fun requireValid(item: LenientDisputeTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDisputeTier>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDisputeTierValidator(): LenientDisputeTierValidator {
    return LenientDisputeTierValidator(
        mutableListOf<LenientDisputeTierRule>(
            LenientDisputeTierRequiredCodeRule("lenient dispute tier needs this field"),
            LenientDisputeTierDriverIdLengthRule(3600),
            LenientDisputeTierRequiredDriverIdRule("lenient dispute tier needs this field"),
            LenientDisputeTierRequiredTitleRule("lenient dispute tier needs a value"),
            LenientDisputeTierAmountRangeRule(5.9, 2152.1),
        ),
    )
}
