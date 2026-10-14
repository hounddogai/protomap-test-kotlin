package com.acme.transport.fleetops.leasereturn.returnforecast

/** A rule a regional lease return manifest must pass. Check returns its problems, or none. */
interface RegionalLeaseReturnManifestRule {
    fun check(item: RegionalLeaseReturnManifest): MutableList<String>
}

data class RegionalLeaseReturnManifestCountryLengthRule(var maxLength: Int) : RegionalLeaseReturnManifestRule {
    override fun check(item: RegionalLeaseReturnManifest): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnManifestRequiredUnitRule(var message: String) : RegionalLeaseReturnManifestRule {
    override fun check(item: RegionalLeaseReturnManifest): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnManifestCodeLengthRule(var maxLength: Int) : RegionalLeaseReturnManifestRule {
    override fun check(item: RegionalLeaseReturnManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnManifestBudgetRangeRule(
    var min: Double,
    var max: Double,
) : RegionalLeaseReturnManifestRule {
    override fun check(item: RegionalLeaseReturnManifest): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnManifestGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : RegionalLeaseReturnManifestRule {
    override fun check(item: RegionalLeaseReturnManifest): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class RegionalLeaseReturnManifestCodePrefixRule(var prefix: String) : RegionalLeaseReturnManifestRule {
    override fun check(item: RegionalLeaseReturnManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional lease return manifest. */
data class RegionalLeaseReturnManifestValidator(var rules: MutableList<RegionalLeaseReturnManifestRule>) {
    fun validate(item: RegionalLeaseReturnManifest): MutableList<String> {
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

    fun requireValid(item: RegionalLeaseReturnManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalLeaseReturnManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalLeaseReturnManifestValidator(): RegionalLeaseReturnManifestValidator {
    return RegionalLeaseReturnManifestValidator(
        mutableListOf<RegionalLeaseReturnManifestRule>(
            RegionalLeaseReturnManifestCountryLengthRule(120),
            RegionalLeaseReturnManifestRequiredUnitRule("regional lease return manifest needs this field"),
            RegionalLeaseReturnManifestCodeLengthRule(3600),
            RegionalLeaseReturnManifestBudgetRangeRule(4.4, 562.7),
            RegionalLeaseReturnManifestGraceDaysRangeRule(64, 3600),
            RegionalLeaseReturnManifestCodePrefixRule("REF"),
        ),
    )
}
