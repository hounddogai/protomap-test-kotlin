package com.acme.transport.emissions.carbonreport.quota

/** A rule a capped carbon report cycle must pass. Check returns its problems, or none. */
interface CappedCarbonReportCycleRule {
    fun check(item: CappedCarbonReportCycle): MutableList<String>
}

data class CappedCarbonReportCycleLabelLengthRule(var maxLength: Int) : CappedCarbonReportCycleRule {
    override fun check(item: CappedCarbonReportCycle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCarbonReportCycleRequiredContactEmailRule(var message: String) : CappedCarbonReportCycleRule {
    override fun check(item: CappedCarbonReportCycle): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCarbonReportCycleRequiredNameRule(var message: String) : CappedCarbonReportCycleRule {
    override fun check(item: CappedCarbonReportCycle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCarbonReportCycleNameLengthRule(var maxLength: Int) : CappedCarbonReportCycleRule {
    override fun check(item: CappedCarbonReportCycle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCarbonReportCycleCodePrefixRule(var prefix: String) : CappedCarbonReportCycleRule {
    override fun check(item: CappedCarbonReportCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped carbon report cycle. */
data class CappedCarbonReportCycleValidator(var rules: MutableList<CappedCarbonReportCycleRule>) {
    fun validate(item: CappedCarbonReportCycle): MutableList<String> {
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

    fun requireValid(item: CappedCarbonReportCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCarbonReportCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCarbonReportCycleValidator(): CappedCarbonReportCycleValidator {
    return CappedCarbonReportCycleValidator(
        mutableListOf<CappedCarbonReportCycleRule>(
            CappedCarbonReportCycleLabelLengthRule(255),
            CappedCarbonReportCycleRequiredContactEmailRule("capped carbon report cycle needs an entry"),
            CappedCarbonReportCycleRequiredNameRule("capped carbon report cycle needs an entry"),
            CappedCarbonReportCycleNameLengthRule(120),
            CappedCarbonReportCycleCodePrefixRule("X"),
        ),
    )
}
