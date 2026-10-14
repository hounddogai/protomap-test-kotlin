package com.acme.transport.lastmile.drop.snapshot

/** A rule a manual drop record must pass. Check returns its problems, or none. */
interface ManualDropRecordRule {
    fun check(item: ManualDropRecord): MutableList<String>
}

data class ManualDropRecordCodeLengthRule(var maxLength: Int) : ManualDropRecordRule {
    override fun check(item: ManualDropRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDropRecordRequiredRegionRule(var message: String) : ManualDropRecordRule {
    override fun check(item: ManualDropRecord): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDropRecordRequiredOwnerIdRule(var message: String) : ManualDropRecordRule {
    override fun check(item: ManualDropRecord): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDropRecordBudgetRangeRule(var min: Double, var max: Double) : ManualDropRecordRule {
    override fun check(item: ManualDropRecord): MutableList<String> {
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

data class ManualDropRecordCreatedAtRangeRule(var min: Int, var max: Int) : ManualDropRecordRule {
    override fun check(item: ManualDropRecord): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual drop record. */
data class ManualDropRecordValidator(var rules: MutableList<ManualDropRecordRule>) {
    fun validate(item: ManualDropRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualDropRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDropRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDropRecordValidator(): ManualDropRecordValidator {
    return ManualDropRecordValidator(
        mutableListOf<ManualDropRecordRule>(
            ManualDropRecordCodeLengthRule(1000),
            ManualDropRecordRequiredRegionRule("manual drop record needs an entry"),
            ManualDropRecordRequiredOwnerIdRule("manual drop record needs this field"),
            ManualDropRecordBudgetRangeRule(0.7, 1978.0),
            ManualDropRecordCreatedAtRangeRule(32, 120),
        ),
    )
}
