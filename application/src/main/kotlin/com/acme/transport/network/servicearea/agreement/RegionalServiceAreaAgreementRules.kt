package com.acme.transport.network.servicearea.agreement

/** A rule a regional service area agreement must pass. Check returns its problems, or none. */
interface RegionalServiceAreaAgreementRule {
    fun check(item: RegionalServiceAreaAgreement): MutableList<String>
}

data class RegionalServiceAreaAgreementRequiredShiftCodeRule(
    var message: String,
) : RegionalServiceAreaAgreementRule {
    override fun check(item: RegionalServiceAreaAgreement): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalServiceAreaAgreementShiftCodeLengthRule(
    var maxLength: Int,
) : RegionalServiceAreaAgreementRule {
    override fun check(item: RegionalServiceAreaAgreement): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalServiceAreaAgreementCodeLengthRule(var maxLength: Int) : RegionalServiceAreaAgreementRule {
    override fun check(item: RegionalServiceAreaAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalServiceAreaAgreementRequiredCodeRule(
    var message: String,
) : RegionalServiceAreaAgreementRule {
    override fun check(item: RegionalServiceAreaAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional service area agreement. */
data class RegionalServiceAreaAgreementValidator(var rules: MutableList<RegionalServiceAreaAgreementRule>) {
    fun validate(item: RegionalServiceAreaAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalServiceAreaAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalServiceAreaAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalServiceAreaAgreementValidator(): RegionalServiceAreaAgreementValidator {
    return RegionalServiceAreaAgreementValidator(
        mutableListOf<RegionalServiceAreaAgreementRule>(
            RegionalServiceAreaAgreementRequiredShiftCodeRule("regional service area agreement needs a value"),
            RegionalServiceAreaAgreementShiftCodeLengthRule(1000),
            RegionalServiceAreaAgreementCodeLengthRule(32),
            RegionalServiceAreaAgreementRequiredCodeRule("regional service area agreement needs this field"),
        ),
    )
}
