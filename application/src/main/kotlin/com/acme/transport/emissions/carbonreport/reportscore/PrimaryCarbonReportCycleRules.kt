package com.acme.transport.emissions.carbonreport.reportscore

/** A rule a primary carbon report cycle must pass. Check returns its problems, or none. */
interface PrimaryCarbonReportCycleRule {
    fun check(item: PrimaryCarbonReportCycle): MutableList<String>
}

data class PrimaryCarbonReportCycleCostRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryCarbonReportCycleRule {
    override fun check(item: PrimaryCarbonReportCycle): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCarbonReportCycleOwnerIdLengthRule(var maxLength: Int) : PrimaryCarbonReportCycleRule {
    override fun check(item: PrimaryCarbonReportCycle): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCarbonReportCycleChecksumLengthRule(var maxLength: Int) : PrimaryCarbonReportCycleRule {
    override fun check(item: PrimaryCarbonReportCycle): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary carbon report cycle. */
data class PrimaryCarbonReportCycleValidator(var rules: MutableList<PrimaryCarbonReportCycleRule>) {
    fun validate(item: PrimaryCarbonReportCycle): MutableList<String> {
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

    fun requireValid(item: PrimaryCarbonReportCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCarbonReportCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCarbonReportCycleValidator(): PrimaryCarbonReportCycleValidator {
    return PrimaryCarbonReportCycleValidator(
        mutableListOf<PrimaryCarbonReportCycleRule>(
            PrimaryCarbonReportCycleCostRangeRule(3.4, 4539.0),
            PrimaryCarbonReportCycleOwnerIdLengthRule(64),
            PrimaryCarbonReportCycleChecksumLengthRule(3600),
        ),
    )
}
