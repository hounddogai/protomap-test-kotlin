package com.acme.transport.network.transittime.cycle

/** A rule a primary transit time adjustment must pass. Check returns its problems, or none. */
interface PrimaryTransitTimeAdjustmentRule {
    fun check(item: PrimaryTransitTimeAdjustment): MutableList<String>
}

data class PrimaryTransitTimeAdjustmentRequiredSegmentRule(
    var message: String,
) : PrimaryTransitTimeAdjustmentRule {
    override fun check(item: PrimaryTransitTimeAdjustment): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTransitTimeAdjustmentCodeLengthRule(var maxLength: Int) : PrimaryTransitTimeAdjustmentRule {
    override fun check(item: PrimaryTransitTimeAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTransitTimeAdjustmentCodePrefixRule(var prefix: String) : PrimaryTransitTimeAdjustmentRule {
    override fun check(item: PrimaryTransitTimeAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTransitTimeAdjustmentQuantityRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryTransitTimeAdjustmentRule {
    override fun check(item: PrimaryTransitTimeAdjustment): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class PrimaryTransitTimeAdjustmentRequiredTitleRule(
    var message: String,
) : PrimaryTransitTimeAdjustmentRule {
    override fun check(item: PrimaryTransitTimeAdjustment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTransitTimeAdjustmentTitleLengthRule(var maxLength: Int) : PrimaryTransitTimeAdjustmentRule {
    override fun check(item: PrimaryTransitTimeAdjustment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary transit time adjustment. */
data class PrimaryTransitTimeAdjustmentValidator(var rules: MutableList<PrimaryTransitTimeAdjustmentRule>) {
    fun validate(item: PrimaryTransitTimeAdjustment): MutableList<String> {
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

    fun requireValid(item: PrimaryTransitTimeAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTransitTimeAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTransitTimeAdjustmentValidator(): PrimaryTransitTimeAdjustmentValidator {
    return PrimaryTransitTimeAdjustmentValidator(
        mutableListOf<PrimaryTransitTimeAdjustmentRule>(
            PrimaryTransitTimeAdjustmentRequiredSegmentRule("primary transit time adjustment needs a value"),
            PrimaryTransitTimeAdjustmentCodeLengthRule(80),
            PrimaryTransitTimeAdjustmentCodePrefixRule("Q"),
            PrimaryTransitTimeAdjustmentQuantityRangeRule(32, 3600),
            PrimaryTransitTimeAdjustmentRequiredTitleRule("primary transit time adjustment needs a value"),
            PrimaryTransitTimeAdjustmentTitleLengthRule(120),
        ),
    )
}
