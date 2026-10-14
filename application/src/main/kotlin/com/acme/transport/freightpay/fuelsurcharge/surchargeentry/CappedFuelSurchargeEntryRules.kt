package com.acme.transport.freightpay.fuelsurcharge.surchargeentry

/** A rule a capped fuel surcharge entry must pass. Check returns its problems, or none. */
interface CappedFuelSurchargeEntryRule {
    fun check(item: CappedFuelSurchargeEntry): MutableList<String>
}

data class CappedFuelSurchargeEntryRequiredLabelRule(var message: String) : CappedFuelSurchargeEntryRule {
    override fun check(item: CappedFuelSurchargeEntry): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedFuelSurchargeEntryLabelLengthRule(var maxLength: Int) : CappedFuelSurchargeEntryRule {
    override fun check(item: CappedFuelSurchargeEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedFuelSurchargeEntryDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : CappedFuelSurchargeEntryRule {
    override fun check(item: CappedFuelSurchargeEntry): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class CappedFuelSurchargeEntryVersionRangeRule(var min: Int, var max: Int) : CappedFuelSurchargeEntryRule {
    override fun check(item: CappedFuelSurchargeEntry): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped fuel surcharge entry. */
data class CappedFuelSurchargeEntryValidator(var rules: MutableList<CappedFuelSurchargeEntryRule>) {
    fun validate(item: CappedFuelSurchargeEntry): MutableList<String> {
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

    fun requireValid(item: CappedFuelSurchargeEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedFuelSurchargeEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedFuelSurchargeEntryValidator(): CappedFuelSurchargeEntryValidator {
    return CappedFuelSurchargeEntryValidator(
        mutableListOf<CappedFuelSurchargeEntryRule>(
            CappedFuelSurchargeEntryRequiredLabelRule("capped fuel surcharge entry needs a value"),
            CappedFuelSurchargeEntryLabelLengthRule(86400),
            CappedFuelSurchargeEntryDistanceKmRangeRule(6.2, 2673.0),
            CappedFuelSurchargeEntryVersionRangeRule(64, 1000),
        ),
    )
}
