package com.acme.transport.fleetops.inspection.batch

/** A rule a pending inspection bundle must pass. Check returns its problems, or none. */
interface PendingInspectionBundleRule {
    fun check(item: PendingInspectionBundle): MutableList<String>
}

data class PendingInspectionBundleCapacityRangeRule(var min: Int, var max: Int) : PendingInspectionBundleRule {
    override fun check(item: PendingInspectionBundle): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionBundleRequiredDescriptionRule(var message: String) : PendingInspectionBundleRule {
    override fun check(item: PendingInspectionBundle): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionBundleRequiredTitleRule(var message: String) : PendingInspectionBundleRule {
    override fun check(item: PendingInspectionBundle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionBundleDescriptionLengthRule(var maxLength: Int) : PendingInspectionBundleRule {
    override fun check(item: PendingInspectionBundle): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionBundleCodeLengthRule(var maxLength: Int) : PendingInspectionBundleRule {
    override fun check(item: PendingInspectionBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionBundleDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : PendingInspectionBundleRule {
    override fun check(item: PendingInspectionBundle): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending inspection bundle. */
data class PendingInspectionBundleValidator(var rules: MutableList<PendingInspectionBundleRule>) {
    fun validate(item: PendingInspectionBundle): MutableList<String> {
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

    fun requireValid(item: PendingInspectionBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingInspectionBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingInspectionBundleValidator(): PendingInspectionBundleValidator {
    return PendingInspectionBundleValidator(
        mutableListOf<PendingInspectionBundleRule>(
            PendingInspectionBundleCapacityRangeRule(16, 16),
            PendingInspectionBundleRequiredDescriptionRule("pending inspection bundle needs a value"),
            PendingInspectionBundleRequiredTitleRule("pending inspection bundle needs this field"),
            PendingInspectionBundleDescriptionLengthRule(255),
            PendingInspectionBundleCodeLengthRule(255),
            PendingInspectionBundleDistanceKmRangeRule(5.3, 1886.8),
        ),
    )
}
