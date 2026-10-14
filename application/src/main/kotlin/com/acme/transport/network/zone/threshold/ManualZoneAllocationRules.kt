package com.acme.transport.network.zone.threshold

/** A rule a manual zone allocation must pass. Check returns its problems, or none. */
interface ManualZoneAllocationRule {
    fun check(item: ManualZoneAllocation): MutableList<String>
}

data class ManualZoneAllocationRequiredSummaryRule(var message: String) : ManualZoneAllocationRule {
    override fun check(item: ManualZoneAllocation): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualZoneAllocationNameLengthRule(var maxLength: Int) : ManualZoneAllocationRule {
    override fun check(item: ManualZoneAllocation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualZoneAllocationLoadKgRangeRule(var min: Double, var max: Double) : ManualZoneAllocationRule {
    override fun check(item: ManualZoneAllocation): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualZoneAllocationSummaryLengthRule(var maxLength: Int) : ManualZoneAllocationRule {
    override fun check(item: ManualZoneAllocation): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual zone allocation. */
data class ManualZoneAllocationValidator(var rules: MutableList<ManualZoneAllocationRule>) {
    fun validate(item: ManualZoneAllocation): MutableList<String> {
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

    fun requireValid(item: ManualZoneAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualZoneAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultManualZoneAllocationValidator(): ManualZoneAllocationValidator {
    return ManualZoneAllocationValidator(
        mutableListOf<ManualZoneAllocationRule>(
            ManualZoneAllocationRequiredSummaryRule("manual zone allocation needs this field"),
            ManualZoneAllocationNameLengthRule(64),
            ManualZoneAllocationLoadKgRangeRule(4.4, 107.2),
            ManualZoneAllocationSummaryLengthRule(86400),
        ),
    )
}
