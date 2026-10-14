package com.acme.transport.lastmile.parcel.reservation

/** A rule a legacy parcel adjustment must pass. Check returns its problems, or none. */
interface LegacyParcelAdjustmentRule {
    fun check(item: LegacyParcelAdjustment): MutableList<String>
}

data class LegacyParcelAdjustmentNameLengthRule(var maxLength: Int) : LegacyParcelAdjustmentRule {
    override fun check(item: LegacyParcelAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyParcelAdjustmentRequiredTrailerIdRule(var message: String) : LegacyParcelAdjustmentRule {
    override fun check(item: LegacyParcelAdjustment): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyParcelAdjustmentVisibleGuardRule(var limit: Int) : LegacyParcelAdjustmentRule {
    override fun check(item: LegacyParcelAdjustment): MutableList<String> {
        if (item.visible && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class LegacyParcelAdjustmentCodePrefixRule(var prefix: String) : LegacyParcelAdjustmentRule {
    override fun check(item: LegacyParcelAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyParcelAdjustmentTrailerIdLengthRule(var maxLength: Int) : LegacyParcelAdjustmentRule {
    override fun check(item: LegacyParcelAdjustment): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy parcel adjustment. */
data class LegacyParcelAdjustmentValidator(var rules: MutableList<LegacyParcelAdjustmentRule>) {
    fun validate(item: LegacyParcelAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyParcelAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyParcelAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyParcelAdjustmentValidator(): LegacyParcelAdjustmentValidator {
    return LegacyParcelAdjustmentValidator(
        mutableListOf<LegacyParcelAdjustmentRule>(
            LegacyParcelAdjustmentNameLengthRule(80),
            LegacyParcelAdjustmentRequiredTrailerIdRule("legacy parcel adjustment needs this field"),
            LegacyParcelAdjustmentVisibleGuardRule(80),
            LegacyParcelAdjustmentCodePrefixRule("OPS"),
            LegacyParcelAdjustmentTrailerIdLengthRule(80),
        ),
    )
}
