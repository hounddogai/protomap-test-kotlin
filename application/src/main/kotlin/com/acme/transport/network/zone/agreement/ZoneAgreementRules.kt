package com.acme.transport.network.zone.agreement

/** A rule a zone agreement must pass. Check returns its problems, or none. */
interface ZoneAgreementRule {
    fun check(item: ZoneAgreement): MutableList<String>
}

data class ZoneAgreementCodeLengthRule(var maxLength: Int) : ZoneAgreementRule {
    override fun check(item: ZoneAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneAgreementRequiredCodeRule(var message: String) : ZoneAgreementRule {
    override fun check(item: ZoneAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneAgreementFloorValueRangeRule(var min: Double, var max: Double) : ZoneAgreementRule {
    override fun check(item: ZoneAgreement): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneAgreementEndsAtRangeRule(var min: Int, var max: Int) : ZoneAgreementRule {
    override fun check(item: ZoneAgreement): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a zone agreement. */
data class ZoneAgreementValidator(var rules: MutableList<ZoneAgreementRule>) {
    fun validate(item: ZoneAgreement): MutableList<String> {
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

    fun requireValid(item: ZoneAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneAgreementValidator(): ZoneAgreementValidator {
    return ZoneAgreementValidator(
        mutableListOf<ZoneAgreementRule>(
            ZoneAgreementCodeLengthRule(3600),
            ZoneAgreementRequiredCodeRule("zone agreement needs this field"),
            ZoneAgreementFloorValueRangeRule(1.9, 2510.1),
            ZoneAgreementEndsAtRangeRule(1000, 3600),
        ),
    )
}
