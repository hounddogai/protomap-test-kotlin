package com.acme.transport.linehaul.capacity.entry

/** A rule a regional capacity manifest must pass. Check returns its problems, or none. */
interface RegionalCapacityManifestRule {
    fun check(item: RegionalCapacityManifest): MutableList<String>
}

data class RegionalCapacityManifestRequiredCodeRule(var message: String) : RegionalCapacityManifestRule {
    override fun check(item: RegionalCapacityManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityManifestRatioRangeRule(
    var min: Double,
    var max: Double,
) : RegionalCapacityManifestRule {
    override fun check(item: RegionalCapacityManifest): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class RegionalCapacityManifestSourceLengthRule(var maxLength: Int) : RegionalCapacityManifestRule {
    override fun check(item: RegionalCapacityManifest): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityManifestRequiredExternalRefRule(var message: String) : RegionalCapacityManifestRule {
    override fun check(item: RegionalCapacityManifest): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityManifestLimitRangeRule(var min: Int, var max: Int) : RegionalCapacityManifestRule {
    override fun check(item: RegionalCapacityManifest): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional capacity manifest. */
data class RegionalCapacityManifestValidator(var rules: MutableList<RegionalCapacityManifestRule>) {
    fun validate(item: RegionalCapacityManifest): MutableList<String> {
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

    fun requireValid(item: RegionalCapacityManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCapacityManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCapacityManifestValidator(): RegionalCapacityManifestValidator {
    return RegionalCapacityManifestValidator(
        mutableListOf<RegionalCapacityManifestRule>(
            RegionalCapacityManifestRequiredCodeRule("regional capacity manifest needs an entry"),
            RegionalCapacityManifestRatioRangeRule(7.2, 2658.1),
            RegionalCapacityManifestSourceLengthRule(64),
            RegionalCapacityManifestRequiredExternalRefRule("regional capacity manifest needs this field"),
            RegionalCapacityManifestLimitRangeRule(1000, 86400),
        ),
    )
}
