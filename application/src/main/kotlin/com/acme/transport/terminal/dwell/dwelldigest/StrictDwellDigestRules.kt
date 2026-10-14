package com.acme.transport.terminal.dwell.dwelldigest

/** A rule a strict dwell digest must pass. Check returns its problems, or none. */
interface StrictDwellDigestRule {
    fun check(item: StrictDwellDigest): MutableList<String>
}

data class StrictDwellDigestRequiredCodeRule(var message: String) : StrictDwellDigestRule {
    override fun check(item: StrictDwellDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDwellDigestRequiredUnitRule(var message: String) : StrictDwellDigestRule {
    override fun check(item: StrictDwellDigest): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDwellDigestFuelLitersRangeRule(var min: Double, var max: Double) : StrictDwellDigestRule {
    override fun check(item: StrictDwellDigest): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class StrictDwellDigestCodeLengthRule(var maxLength: Int) : StrictDwellDigestRule {
    override fun check(item: StrictDwellDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDwellDigestCodePrefixRule(var prefix: String) : StrictDwellDigestRule {
    override fun check(item: StrictDwellDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict dwell digest. */
data class StrictDwellDigestValidator(var rules: MutableList<StrictDwellDigestRule>) {
    fun validate(item: StrictDwellDigest): MutableList<String> {
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

    fun requireValid(item: StrictDwellDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDwellDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDwellDigestValidator(): StrictDwellDigestValidator {
    return StrictDwellDigestValidator(
        mutableListOf<StrictDwellDigestRule>(
            StrictDwellDigestRequiredCodeRule("strict dwell digest needs this field"),
            StrictDwellDigestRequiredUnitRule("strict dwell digest needs this field"),
            StrictDwellDigestFuelLitersRangeRule(4.9, 160.9),
            StrictDwellDigestCodeLengthRule(80),
            StrictDwellDigestCodePrefixRule("ACM"),
        ),
    )
}
