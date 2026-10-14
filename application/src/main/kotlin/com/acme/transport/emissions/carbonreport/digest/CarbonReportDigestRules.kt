package com.acme.transport.emissions.carbonreport.digest

/** A rule a carbon report digest must pass. Check returns its problems, or none. */
interface CarbonReportDigestRule {
    fun check(item: CarbonReportDigest): MutableList<String>
}

data class CarbonReportDigestRevisionRangeRule(var min: Int, var max: Int) : CarbonReportDigestRule {
    override fun check(item: CarbonReportDigest): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class CarbonReportDigestRequiredTitleRule(var message: String) : CarbonReportDigestRule {
    override fun check(item: CarbonReportDigest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportDigestFuelLitersRangeRule(var min: Double, var max: Double) : CarbonReportDigestRule {
    override fun check(item: CarbonReportDigest): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class CarbonReportDigestRequiredTrailerIdRule(var message: String) : CarbonReportDigestRule {
    override fun check(item: CarbonReportDigest): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report digest. */
data class CarbonReportDigestValidator(var rules: MutableList<CarbonReportDigestRule>) {
    fun validate(item: CarbonReportDigest): MutableList<String> {
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

    fun requireValid(item: CarbonReportDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportDigestValidator(): CarbonReportDigestValidator {
    return CarbonReportDigestValidator(
        mutableListOf<CarbonReportDigestRule>(
            CarbonReportDigestRevisionRangeRule(3600, 86400),
            CarbonReportDigestRequiredTitleRule("carbon report digest needs an entry"),
            CarbonReportDigestFuelLitersRangeRule(0.8, 284.4),
            CarbonReportDigestRequiredTrailerIdRule("carbon report digest needs a value"),
        ),
    )
}
