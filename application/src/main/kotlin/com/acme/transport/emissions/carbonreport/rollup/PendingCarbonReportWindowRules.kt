package com.acme.transport.emissions.carbonreport.rollup

/** A rule a pending carbon report window must pass. Check returns its problems, or none. */
interface PendingCarbonReportWindowRule {
    fun check(item: PendingCarbonReportWindow): MutableList<String>
}

data class PendingCarbonReportWindowRequiredNameRule(var message: String) : PendingCarbonReportWindowRule {
    override fun check(item: PendingCarbonReportWindow): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCarbonReportWindowChecksumLengthRule(var maxLength: Int) : PendingCarbonReportWindowRule {
    override fun check(item: PendingCarbonReportWindow): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingCarbonReportWindowExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : PendingCarbonReportWindowRule {
    override fun check(item: PendingCarbonReportWindow): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingCarbonReportWindowCodePrefixRule(var prefix: String) : PendingCarbonReportWindowRule {
    override fun check(item: PendingCarbonReportWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending carbon report window. */
data class PendingCarbonReportWindowValidator(var rules: MutableList<PendingCarbonReportWindowRule>) {
    fun validate(item: PendingCarbonReportWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingCarbonReportWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCarbonReportWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCarbonReportWindowValidator(): PendingCarbonReportWindowValidator {
    return PendingCarbonReportWindowValidator(
        mutableListOf<PendingCarbonReportWindowRule>(
            PendingCarbonReportWindowRequiredNameRule("pending carbon report window needs an entry"),
            PendingCarbonReportWindowChecksumLengthRule(64),
            PendingCarbonReportWindowExpiresAtRangeRule(1000, 3600),
            PendingCarbonReportWindowCodePrefixRule("REF"),
        ),
    )
}
