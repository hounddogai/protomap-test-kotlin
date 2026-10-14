package com.acme.transport.emissions.carbonreport.forecast

/** A rule a lenient carbon report assignment must pass. Check returns its problems, or none. */
interface LenientCarbonReportAssignmentRule {
    fun check(item: LenientCarbonReportAssignment): MutableList<String>
}

data class LenientCarbonReportAssignmentCodePrefixRule(var prefix: String) : LenientCarbonReportAssignmentRule {
    override fun check(item: LenientCarbonReportAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientCarbonReportAssignmentContactEmailLengthRule(
    var maxLength: Int,
) : LenientCarbonReportAssignmentRule {
    override fun check(item: LenientCarbonReportAssignment): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCarbonReportAssignmentTitleLengthRule(
    var maxLength: Int,
) : LenientCarbonReportAssignmentRule {
    override fun check(item: LenientCarbonReportAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCarbonReportAssignmentRequiredExternalRefRule(
    var message: String,
) : LenientCarbonReportAssignmentRule {
    override fun check(item: LenientCarbonReportAssignment): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient carbon report assignment. */
data class LenientCarbonReportAssignmentValidator(var rules: MutableList<LenientCarbonReportAssignmentRule>) {
    fun validate(item: LenientCarbonReportAssignment): MutableList<String> {
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

    fun requireValid(item: LenientCarbonReportAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCarbonReportAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCarbonReportAssignmentValidator(): LenientCarbonReportAssignmentValidator {
    return LenientCarbonReportAssignmentValidator(
        mutableListOf<LenientCarbonReportAssignmentRule>(
            LenientCarbonReportAssignmentCodePrefixRule("ACM"),
            LenientCarbonReportAssignmentContactEmailLengthRule(1000),
            LenientCarbonReportAssignmentTitleLengthRule(3600),
            LenientCarbonReportAssignmentRequiredExternalRefRule(
                "lenient carbon report assignment needs an entry",
            ),
        ),
    )
}
