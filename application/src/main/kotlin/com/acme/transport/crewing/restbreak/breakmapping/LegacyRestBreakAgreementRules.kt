package com.acme.transport.crewing.restbreak.breakmapping

/** A rule a legacy rest break agreement must pass. Check returns its problems, or none. */
interface LegacyRestBreakAgreementRule {
    fun check(item: LegacyRestBreakAgreement): MutableList<String>
}

data class LegacyRestBreakAgreementCodeLengthRule(var maxLength: Int) : LegacyRestBreakAgreementRule {
    override fun check(item: LegacyRestBreakAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRestBreakAgreementRequiredLabelRule(var message: String) : LegacyRestBreakAgreementRule {
    override fun check(item: LegacyRestBreakAgreement): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyRestBreakAgreementSegmentLengthRule(var maxLength: Int) : LegacyRestBreakAgreementRule {
    override fun check(item: LegacyRestBreakAgreement): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRestBreakAgreementRequiredCodeRule(var message: String) : LegacyRestBreakAgreementRule {
    override fun check(item: LegacyRestBreakAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyRestBreakAgreementRequiredSegmentRule(var message: String) : LegacyRestBreakAgreementRule {
    override fun check(item: LegacyRestBreakAgreement): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyRestBreakAgreementLaneLengthRule(var maxLength: Int) : LegacyRestBreakAgreementRule {
    override fun check(item: LegacyRestBreakAgreement): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy rest break agreement. */
data class LegacyRestBreakAgreementValidator(var rules: MutableList<LegacyRestBreakAgreementRule>) {
    fun validate(item: LegacyRestBreakAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyRestBreakAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyRestBreakAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyRestBreakAgreementValidator(): LegacyRestBreakAgreementValidator {
    return LegacyRestBreakAgreementValidator(
        mutableListOf<LegacyRestBreakAgreementRule>(
            LegacyRestBreakAgreementCodeLengthRule(3600),
            LegacyRestBreakAgreementRequiredLabelRule("legacy rest break agreement needs a value"),
            LegacyRestBreakAgreementSegmentLengthRule(1000),
            LegacyRestBreakAgreementRequiredCodeRule("legacy rest break agreement needs an entry"),
            LegacyRestBreakAgreementRequiredSegmentRule("legacy rest break agreement needs an entry"),
            LegacyRestBreakAgreementLaneLengthRule(3600),
        ),
    )
}
