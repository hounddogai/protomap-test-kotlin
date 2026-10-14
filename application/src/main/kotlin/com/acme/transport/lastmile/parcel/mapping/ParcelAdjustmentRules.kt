package com.acme.transport.lastmile.parcel.mapping

/** A rule a parcel adjustment must pass. Check returns its problems, or none. */
interface ParcelAdjustmentRule {
    fun check(item: ParcelAdjustment): MutableList<String>
}

data class ParcelAdjustmentCodeLengthRule(var maxLength: Int) : ParcelAdjustmentRule {
    override fun check(item: ParcelAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelAdjustmentNameLengthRule(var maxLength: Int) : ParcelAdjustmentRule {
    override fun check(item: ParcelAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelAdjustmentRegionLengthRule(var maxLength: Int) : ParcelAdjustmentRule {
    override fun check(item: ParcelAdjustment): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelAdjustmentMarginRangeRule(var min: Double, var max: Double) : ParcelAdjustmentRule {
    override fun check(item: ParcelAdjustment): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ParcelAdjustmentLoadKgRangeRule(var min: Double, var max: Double) : ParcelAdjustmentRule {
    override fun check(item: ParcelAdjustment): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel adjustment. */
data class ParcelAdjustmentValidator(var rules: MutableList<ParcelAdjustmentRule>) {
    fun validate(item: ParcelAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelAdjustmentValidator(): ParcelAdjustmentValidator {
    return ParcelAdjustmentValidator(
        mutableListOf<ParcelAdjustmentRule>(
            ParcelAdjustmentCodeLengthRule(3600),
            ParcelAdjustmentNameLengthRule(64),
            ParcelAdjustmentRegionLengthRule(3600),
            ParcelAdjustmentMarginRangeRule(2.9, 4154.8),
            ParcelAdjustmentLoadKgRangeRule(2.2, 1002.6),
        ),
    )
}
