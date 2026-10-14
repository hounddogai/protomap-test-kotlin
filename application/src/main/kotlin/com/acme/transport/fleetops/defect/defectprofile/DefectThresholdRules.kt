package com.acme.transport.fleetops.defect.defectprofile

/** A rule a defect threshold must pass. Check returns its problems, or none. */
interface DefectThresholdRule {
    fun check(item: DefectThreshold): MutableList<String>
}

data class DefectThresholdWindowDaysRangeRule(var min: Int, var max: Int) : DefectThresholdRule {
    override fun check(item: DefectThreshold): MutableList<String> {
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

data class DefectThresholdRegionLengthRule(var maxLength: Int) : DefectThresholdRule {
    override fun check(item: DefectThreshold): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectThresholdRequiredCodeRule(var message: String) : DefectThresholdRule {
    override fun check(item: DefectThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectThresholdEtaMinutesRangeRule(var min: Int, var max: Int) : DefectThresholdRule {
    override fun check(item: DefectThreshold): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a defect threshold. */
data class DefectThresholdValidator(var rules: MutableList<DefectThresholdRule>) {
    fun validate(item: DefectThreshold): MutableList<String> {
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

    fun requireValid(item: DefectThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectThresholdValidator(): DefectThresholdValidator {
    return DefectThresholdValidator(
        mutableListOf<DefectThresholdRule>(
            DefectThresholdWindowDaysRangeRule(32, 120),
            DefectThresholdRegionLengthRule(255),
            DefectThresholdRequiredCodeRule("defect threshold needs an entry"),
            DefectThresholdEtaMinutesRangeRule(3600, 86400),
        ),
    )
}
