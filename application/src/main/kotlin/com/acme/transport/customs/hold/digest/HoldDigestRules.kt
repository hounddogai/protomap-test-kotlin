package com.acme.transport.customs.hold.digest

/** A rule a hold digest must pass. Check returns its problems, or none. */
interface HoldDigestRule {
    fun check(item: HoldDigest): MutableList<String>
}

data class HoldDigestRequiredVehicleIdRule(var message: String) : HoldDigestRule {
    override fun check(item: HoldDigest): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldDigestLabelLengthRule(var maxLength: Int) : HoldDigestRule {
    override fun check(item: HoldDigest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldDigestRequiredLaneRule(var message: String) : HoldDigestRule {
    override fun check(item: HoldDigest): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldDigestWindowDaysRangeRule(var min: Int, var max: Int) : HoldDigestRule {
    override fun check(item: HoldDigest): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class HoldDigestLaneLengthRule(var maxLength: Int) : HoldDigestRule {
    override fun check(item: HoldDigest): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold digest. */
data class HoldDigestValidator(var rules: MutableList<HoldDigestRule>) {
    fun validate(item: HoldDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HoldDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldDigestValidator(): HoldDigestValidator {
    return HoldDigestValidator(
        mutableListOf<HoldDigestRule>(
            HoldDigestRequiredVehicleIdRule("hold digest needs a value"),
            HoldDigestLabelLengthRule(3600),
            HoldDigestRequiredLaneRule("hold digest needs a value"),
            HoldDigestWindowDaysRangeRule(32, 3600),
            HoldDigestLaneLengthRule(32),
        ),
    )
}
