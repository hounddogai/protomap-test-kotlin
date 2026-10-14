package com.acme.transport.network.hub.primaryhubsummary

/** A rule a regional hub snapshot must pass. Check returns its problems, or none. */
interface RegionalHubSnapshotRule {
    fun check(item: RegionalHubSnapshot): MutableList<String>
}

data class RegionalHubSnapshotVisibleGuardRule(var limit: Int) : RegionalHubSnapshotRule {
    override fun check(item: RegionalHubSnapshot): MutableList<String> {
        if (item.visible && item.pallets > limit) {
            return mutableListOf<String>("pallets ${item.pallets} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class RegionalHubSnapshotNameLengthRule(var maxLength: Int) : RegionalHubSnapshotRule {
    override fun check(item: RegionalHubSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalHubSnapshotBudgetRangeRule(var min: Double, var max: Double) : RegionalHubSnapshotRule {
    override fun check(item: RegionalHubSnapshot): MutableList<String> {
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

data class RegionalHubSnapshotPalletsRangeRule(var min: Int, var max: Int) : RegionalHubSnapshotRule {
    override fun check(item: RegionalHubSnapshot): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional hub snapshot. */
data class RegionalHubSnapshotValidator(var rules: MutableList<RegionalHubSnapshotRule>) {
    fun validate(item: RegionalHubSnapshot): MutableList<String> {
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

    fun requireValid(item: RegionalHubSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalHubSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalHubSnapshotValidator(): RegionalHubSnapshotValidator {
    return RegionalHubSnapshotValidator(
        mutableListOf<RegionalHubSnapshotRule>(
            RegionalHubSnapshotVisibleGuardRule(32),
            RegionalHubSnapshotNameLengthRule(16),
            RegionalHubSnapshotBudgetRangeRule(4.9, 2109.3),
            RegionalHubSnapshotPalletsRangeRule(1000, 86400),
        ),
    )
}
