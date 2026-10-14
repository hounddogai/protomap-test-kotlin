package com.acme.transport.emissions.carbonreport.reservation

import com.acme.transport.kit.text.normalize

/** A rule a carbon report reservation must pass. Check returns its problems, or none. */
interface CarbonReportReservationRule {
    fun check(item: CarbonReportReservation): MutableList<String>
}

data class CarbonReportReservationChannelLengthRule(var maxLength: Int) : CarbonReportReservationRule {
    override fun check(item: CarbonReportReservation): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportReservationDriverIdLengthRule(var maxLength: Int) : CarbonReportReservationRule {
    override fun check(item: CarbonReportReservation): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportReservationCodeLengthRule(var maxLength: Int) : CarbonReportReservationRule {
    override fun check(item: CarbonReportReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportReservationAllowedCodeRule(
    var allowed: MutableList<String>,
) : CarbonReportReservationRule {
    override fun check(item: CarbonReportReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportReservationRequiredCodeRule(var message: String) : CarbonReportReservationRule {
    override fun check(item: CarbonReportReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report reservation. */
data class CarbonReportReservationValidator(var rules: MutableList<CarbonReportReservationRule>) {
    fun validate(item: CarbonReportReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CarbonReportReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportReservationValidator(): CarbonReportReservationValidator {
    return CarbonReportReservationValidator(
        mutableListOf<CarbonReportReservationRule>(
            CarbonReportReservationChannelLengthRule(255),
            CarbonReportReservationDriverIdLengthRule(3600),
            CarbonReportReservationCodeLengthRule(16),
            CarbonReportReservationAllowedCodeRule(mutableListOf<String>("adaptive", "tiered")),
            CarbonReportReservationRequiredCodeRule("carbon report reservation needs this field"),
        ),
    )
}
