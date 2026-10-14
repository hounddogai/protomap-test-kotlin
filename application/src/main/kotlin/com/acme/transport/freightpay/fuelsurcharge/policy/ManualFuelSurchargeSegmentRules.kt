package com.acme.transport.freightpay.fuelsurcharge.policy

/** A rule a manual fuel surcharge segment must pass. Check returns its problems, or none. */
interface ManualFuelSurchargeSegmentRule {
    fun check(item: ManualFuelSurchargeSegment): MutableList<String>
}

data class ManualFuelSurchargeSegmentRequiredChannelRule(var message: String) : ManualFuelSurchargeSegmentRule {
    override fun check(item: ManualFuelSurchargeSegment): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelSurchargeSegmentMinimumRangeRule(
    var min: Double,
    var max: Double,
) : ManualFuelSurchargeSegmentRule {
    override fun check(item: ManualFuelSurchargeSegment): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class ManualFuelSurchargeSegmentTitleLengthRule(var maxLength: Int) : ManualFuelSurchargeSegmentRule {
    override fun check(item: ManualFuelSurchargeSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelSurchargeSegmentCodePrefixRule(var prefix: String) : ManualFuelSurchargeSegmentRule {
    override fun check(item: ManualFuelSurchargeSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual fuel surcharge segment. */
data class ManualFuelSurchargeSegmentValidator(var rules: MutableList<ManualFuelSurchargeSegmentRule>) {
    fun validate(item: ManualFuelSurchargeSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualFuelSurchargeSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualFuelSurchargeSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultManualFuelSurchargeSegmentValidator(): ManualFuelSurchargeSegmentValidator {
    return ManualFuelSurchargeSegmentValidator(
        mutableListOf<ManualFuelSurchargeSegmentRule>(
            ManualFuelSurchargeSegmentRequiredChannelRule("manual fuel surcharge segment needs this field"),
            ManualFuelSurchargeSegmentMinimumRangeRule(4.8, 4551.1),
            ManualFuelSurchargeSegmentTitleLengthRule(16),
            ManualFuelSurchargeSegmentCodePrefixRule("X"),
        ),
    )
}
