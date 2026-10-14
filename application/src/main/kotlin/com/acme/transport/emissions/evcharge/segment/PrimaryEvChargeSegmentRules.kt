package com.acme.transport.emissions.evcharge.segment

/** A rule a primary ev charge segment must pass. Check returns its problems, or none. */
interface PrimaryEvChargeSegmentRule {
    fun check(item: PrimaryEvChargeSegment): MutableList<String>
}

data class PrimaryEvChargeSegmentDriverIdLengthRule(var maxLength: Int) : PrimaryEvChargeSegmentRule {
    override fun check(item: PrimaryEvChargeSegment): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryEvChargeSegmentRequiredDriverIdRule(var message: String) : PrimaryEvChargeSegmentRule {
    override fun check(item: PrimaryEvChargeSegment): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryEvChargeSegmentCodeLengthRule(var maxLength: Int) : PrimaryEvChargeSegmentRule {
    override fun check(item: PrimaryEvChargeSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary ev charge segment. */
data class PrimaryEvChargeSegmentValidator(var rules: MutableList<PrimaryEvChargeSegmentRule>) {
    fun validate(item: PrimaryEvChargeSegment): MutableList<String> {
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

    fun requireValid(item: PrimaryEvChargeSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryEvChargeSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryEvChargeSegmentValidator(): PrimaryEvChargeSegmentValidator {
    return PrimaryEvChargeSegmentValidator(
        mutableListOf<PrimaryEvChargeSegmentRule>(
            PrimaryEvChargeSegmentDriverIdLengthRule(32),
            PrimaryEvChargeSegmentRequiredDriverIdRule("primary ev charge segment needs this field"),
            PrimaryEvChargeSegmentCodeLengthRule(86400),
        ),
    )
}
