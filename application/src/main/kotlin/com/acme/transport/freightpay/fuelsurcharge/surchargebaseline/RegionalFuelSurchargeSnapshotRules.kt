package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

/** A rule a regional fuel surcharge snapshot must pass. Check returns its problems, or none. */
interface RegionalFuelSurchargeSnapshotRule {
    fun check(item: RegionalFuelSurchargeSnapshot): MutableList<String>
}

data class RegionalFuelSurchargeSnapshotNameLengthRule(var maxLength: Int) : RegionalFuelSurchargeSnapshotRule {
    override fun check(item: RegionalFuelSurchargeSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelSurchargeSnapshotRequiredLocaleRule(
    var message: String,
) : RegionalFuelSurchargeSnapshotRule {
    override fun check(item: RegionalFuelSurchargeSnapshot): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelSurchargeSnapshotRequiredLaneRule(
    var message: String,
) : RegionalFuelSurchargeSnapshotRule {
    override fun check(item: RegionalFuelSurchargeSnapshot): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional fuel surcharge snapshot. */
data class RegionalFuelSurchargeSnapshotValidator(var rules: MutableList<RegionalFuelSurchargeSnapshotRule>) {
    fun validate(item: RegionalFuelSurchargeSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalFuelSurchargeSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalFuelSurchargeSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalFuelSurchargeSnapshotValidator(): RegionalFuelSurchargeSnapshotValidator {
    return RegionalFuelSurchargeSnapshotValidator(
        mutableListOf<RegionalFuelSurchargeSnapshotRule>(
            RegionalFuelSurchargeSnapshotNameLengthRule(32),
            RegionalFuelSurchargeSnapshotRequiredLocaleRule("regional fuel surcharge snapshot needs this field"),
            RegionalFuelSurchargeSnapshotRequiredLaneRule("regional fuel surcharge snapshot needs this field"),
        ),
    )
}
