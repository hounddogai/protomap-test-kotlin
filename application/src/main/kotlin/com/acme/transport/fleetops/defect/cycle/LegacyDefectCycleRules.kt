package com.acme.transport.fleetops.defect.cycle

/** A rule a legacy defect cycle must pass. Check returns its problems, or none. */
interface LegacyDefectCycleRule {
    fun check(item: LegacyDefectCycle): MutableList<String>
}

data class LegacyDefectCyclePhoneLengthRule(var maxLength: Int) : LegacyDefectCycleRule {
    override fun check(item: LegacyDefectCycle): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDefectCycleRequiredCodeRule(var message: String) : LegacyDefectCycleRule {
    override fun check(item: LegacyDefectCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDefectCycleDriverIdLengthRule(var maxLength: Int) : LegacyDefectCycleRule {
    override fun check(item: LegacyDefectCycle): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy defect cycle. */
data class LegacyDefectCycleValidator(var rules: MutableList<LegacyDefectCycleRule>) {
    fun validate(item: LegacyDefectCycle): MutableList<String> {
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

    fun requireValid(item: LegacyDefectCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDefectCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDefectCycleValidator(): LegacyDefectCycleValidator {
    return LegacyDefectCycleValidator(
        mutableListOf<LegacyDefectCycleRule>(
            LegacyDefectCyclePhoneLengthRule(80),
            LegacyDefectCycleRequiredCodeRule("legacy defect cycle needs an entry"),
            LegacyDefectCycleDriverIdLengthRule(255),
        ),
    )
}
