package com.acme.transport.lastmile.locker.lockerclaim

import com.acme.transport.kit.text.normalize

/** A rule a locker bundle must pass. Check returns its problems, or none. */
interface LockerBundleRule {
    fun check(item: LockerBundle): MutableList<String>
}

data class LockerBundleAllowedShiftCodeRule(var allowed: MutableList<String>) : LockerBundleRule {
    override fun check(item: LockerBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class LockerBundleCodeLengthRule(var maxLength: Int) : LockerBundleRule {
    override fun check(item: LockerBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerBundleRatioRangeRule(var min: Double, var max: Double) : LockerBundleRule {
    override fun check(item: LockerBundle): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerBundleEtaMinutesRangeRule(var min: Int, var max: Int) : LockerBundleRule {
    override fun check(item: LockerBundle): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a locker bundle. */
data class LockerBundleValidator(var rules: MutableList<LockerBundleRule>) {
    fun validate(item: LockerBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LockerBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerBundleValidator(): LockerBundleValidator {
    return LockerBundleValidator(
        mutableListOf<LockerBundleRule>(
            LockerBundleAllowedShiftCodeRule(mutableListOf<String>("batched")),
            LockerBundleCodeLengthRule(64),
            LockerBundleRatioRangeRule(0.5, 2365.1),
            LockerBundleEtaMinutesRangeRule(16, 1000),
        ),
    )
}
