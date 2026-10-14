package com.acme.transport.freightpay.fuelsurcharge.exception

/** A rule a fuel surcharge segment must pass. Check returns its problems, or none. */
interface FuelSurchargeSegmentRule {
    fun check(item: FuelSurchargeSegment): MutableList<String>
}

data class FuelSurchargeSegmentRegionLengthRule(var maxLength: Int) : FuelSurchargeSegmentRule {
    override fun check(item: FuelSurchargeSegment): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeSegmentWindowDaysRangeRule(var min: Int, var max: Int) : FuelSurchargeSegmentRule {
    override fun check(item: FuelSurchargeSegment): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeSegmentRequiredRegionRule(var message: String) : FuelSurchargeSegmentRule {
    override fun check(item: FuelSurchargeSegment): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeSegmentChecksumLengthRule(var maxLength: Int) : FuelSurchargeSegmentRule {
    override fun check(item: FuelSurchargeSegment): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeSegmentRequiredNameRule(var message: String) : FuelSurchargeSegmentRule {
    override fun check(item: FuelSurchargeSegment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge segment. */
data class FuelSurchargeSegmentValidator(var rules: MutableList<FuelSurchargeSegmentRule>) {
    fun validate(item: FuelSurchargeSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelSurchargeSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeSegmentValidator(): FuelSurchargeSegmentValidator {
    return FuelSurchargeSegmentValidator(
        mutableListOf<FuelSurchargeSegmentRule>(
            FuelSurchargeSegmentRegionLengthRule(86400),
            FuelSurchargeSegmentWindowDaysRangeRule(32, 3600),
            FuelSurchargeSegmentRequiredRegionRule("fuel surcharge segment needs a value"),
            FuelSurchargeSegmentChecksumLengthRule(86400),
            FuelSurchargeSegmentRequiredNameRule("fuel surcharge segment needs a value"),
        ),
    )
}
