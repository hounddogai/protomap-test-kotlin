package com.acme.transport.linehaul.capacity.capacitytarget

/** A rule a regional capacity rollup must pass. Check returns its problems, or none. */
interface RegionalCapacityRollupRule {
    fun check(item: RegionalCapacityRollup): MutableList<String>
}

data class RegionalCapacityRollupLabelLengthRule(var maxLength: Int) : RegionalCapacityRollupRule {
    override fun check(item: RegionalCapacityRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityRollupUnitLengthRule(var maxLength: Int) : RegionalCapacityRollupRule {
    override fun check(item: RegionalCapacityRollup): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityRollupWindowDaysRangeRule(var min: Int, var max: Int) : RegionalCapacityRollupRule {
    override fun check(item: RegionalCapacityRollup): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class RegionalCapacityRollupRequiredLabelRule(var message: String) : RegionalCapacityRollupRule {
    override fun check(item: RegionalCapacityRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityRollupOwnerIdLengthRule(var maxLength: Int) : RegionalCapacityRollupRule {
    override fun check(item: RegionalCapacityRollup): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityRollupRequiredCodeRule(var message: String) : RegionalCapacityRollupRule {
    override fun check(item: RegionalCapacityRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional capacity rollup. */
data class RegionalCapacityRollupValidator(var rules: MutableList<RegionalCapacityRollupRule>) {
    fun validate(item: RegionalCapacityRollup): MutableList<String> {
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

    fun requireValid(item: RegionalCapacityRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCapacityRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCapacityRollupValidator(): RegionalCapacityRollupValidator {
    return RegionalCapacityRollupValidator(
        mutableListOf<RegionalCapacityRollupRule>(
            RegionalCapacityRollupLabelLengthRule(1000),
            RegionalCapacityRollupUnitLengthRule(255),
            RegionalCapacityRollupWindowDaysRangeRule(32, 80),
            RegionalCapacityRollupRequiredLabelRule("regional capacity rollup needs a value"),
            RegionalCapacityRollupOwnerIdLengthRule(80),
            RegionalCapacityRollupRequiredCodeRule("regional capacity rollup needs this field"),
        ),
    )
}
