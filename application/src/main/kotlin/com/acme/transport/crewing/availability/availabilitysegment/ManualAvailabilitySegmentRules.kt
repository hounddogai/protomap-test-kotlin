package com.acme.transport.crewing.availability.availabilitysegment

/** A rule a manual availability segment must pass. Check returns its problems, or none. */
interface ManualAvailabilitySegmentRule {
    fun check(item: ManualAvailabilitySegment): MutableList<String>
}

data class ManualAvailabilitySegmentOdometerRangeRule(
    var min: Int,
    var max: Int,
) : ManualAvailabilitySegmentRule {
    override fun check(item: ManualAvailabilitySegment): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class ManualAvailabilitySegmentCodePrefixRule(var prefix: String) : ManualAvailabilitySegmentRule {
    override fun check(item: ManualAvailabilitySegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualAvailabilitySegmentRequiredShiftCodeRule(var message: String) : ManualAvailabilitySegmentRule {
    override fun check(item: ManualAvailabilitySegment): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualAvailabilitySegmentPriorityRangeRule(
    var min: Int,
    var max: Int,
) : ManualAvailabilitySegmentRule {
    override fun check(item: ManualAvailabilitySegment): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualAvailabilitySegmentCodeLengthRule(var maxLength: Int) : ManualAvailabilitySegmentRule {
    override fun check(item: ManualAvailabilitySegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual availability segment. */
data class ManualAvailabilitySegmentValidator(var rules: MutableList<ManualAvailabilitySegmentRule>) {
    fun validate(item: ManualAvailabilitySegment): MutableList<String> {
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

    fun requireValid(item: ManualAvailabilitySegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAvailabilitySegment>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAvailabilitySegmentValidator(): ManualAvailabilitySegmentValidator {
    return ManualAvailabilitySegmentValidator(
        mutableListOf<ManualAvailabilitySegmentRule>(
            ManualAvailabilitySegmentOdometerRangeRule(16, 32),
            ManualAvailabilitySegmentCodePrefixRule("ACM"),
            ManualAvailabilitySegmentRequiredShiftCodeRule("manual availability segment needs a value"),
            ManualAvailabilitySegmentPriorityRangeRule(64, 86400),
            ManualAvailabilitySegmentCodeLengthRule(86400),
        ),
    )
}
