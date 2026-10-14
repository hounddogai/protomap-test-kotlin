package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.kit.text.normalize

/** A rule a capped fuel burn manifest must pass. Check returns its problems, or none. */
interface CappedFuelBurnManifestRule {
    fun check(item: CappedFuelBurnManifest): MutableList<String>
}

data class CappedFuelBurnManifestRequiredTitleRule(var message: String) : CappedFuelBurnManifestRule {
    override fun check(item: CappedFuelBurnManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedFuelBurnManifestFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : CappedFuelBurnManifestRule {
    override fun check(item: CappedFuelBurnManifest): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedFuelBurnManifestRequiredShiftCodeRule(var message: String) : CappedFuelBurnManifestRule {
    override fun check(item: CappedFuelBurnManifest): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedFuelBurnManifestTitleLengthRule(var maxLength: Int) : CappedFuelBurnManifestRule {
    override fun check(item: CappedFuelBurnManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedFuelBurnManifestAllowedSummaryRule(
    var allowed: MutableList<String>,
) : CappedFuelBurnManifestRule {
    override fun check(item: CappedFuelBurnManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is stale")
        }
        return mutableListOf<String>()
    }
}

data class CappedFuelBurnManifestRequiredCodeRule(var message: String) : CappedFuelBurnManifestRule {
    override fun check(item: CappedFuelBurnManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped fuel burn manifest. */
data class CappedFuelBurnManifestValidator(var rules: MutableList<CappedFuelBurnManifestRule>) {
    fun validate(item: CappedFuelBurnManifest): MutableList<String> {
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

    fun requireValid(item: CappedFuelBurnManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedFuelBurnManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedFuelBurnManifestValidator(): CappedFuelBurnManifestValidator {
    return CappedFuelBurnManifestValidator(
        mutableListOf<CappedFuelBurnManifestRule>(
            CappedFuelBurnManifestRequiredTitleRule("capped fuel burn manifest needs a value"),
            CappedFuelBurnManifestFloorValueRangeRule(7.6, 3356.7),
            CappedFuelBurnManifestRequiredShiftCodeRule("capped fuel burn manifest needs a value"),
            CappedFuelBurnManifestTitleLengthRule(16),
            CappedFuelBurnManifestAllowedSummaryRule(mutableListOf<String>("manual")),
            CappedFuelBurnManifestRequiredCodeRule("capped fuel burn manifest needs a value"),
        ),
    )
}
