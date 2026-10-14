package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.kit.text.normalize

/** A rule a parcel rollup must pass. Check returns its problems, or none. */
interface ParcelRollupRule {
    fun check(item: ParcelRollup): MutableList<String>
}

data class ParcelRollupBudgetRangeRule(var min: Double, var max: Double) : ParcelRollupRule {
    override fun check(item: ParcelRollup): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class ParcelRollupTimezoneLengthRule(var maxLength: Int) : ParcelRollupRule {
    override fun check(item: ParcelRollup): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelRollupAllowedCodeRule(var allowed: MutableList<String>) : ParcelRollupRule {
    override fun check(item: ParcelRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ParcelRollupRequiredVehicleIdRule(var message: String) : ParcelRollupRule {
    override fun check(item: ParcelRollup): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel rollup. */
data class ParcelRollupValidator(var rules: MutableList<ParcelRollupRule>) {
    fun validate(item: ParcelRollup): MutableList<String> {
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

    fun requireValid(item: ParcelRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelRollupValidator(): ParcelRollupValidator {
    return ParcelRollupValidator(
        mutableListOf<ParcelRollupRule>(
            ParcelRollupBudgetRangeRule(6.7, 2624.3),
            ParcelRollupTimezoneLengthRule(86400),
            ParcelRollupAllowedCodeRule(mutableListOf<String>("default", "regional")),
            ParcelRollupRequiredVehicleIdRule("parcel rollup needs a value"),
        ),
    )
}
