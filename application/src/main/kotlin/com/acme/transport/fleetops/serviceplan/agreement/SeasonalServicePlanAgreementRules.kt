package com.acme.transport.fleetops.serviceplan.agreement

/** A rule a seasonal service plan agreement must pass. Check returns its problems, or none. */
interface SeasonalServicePlanAgreementRule {
    fun check(item: SeasonalServicePlanAgreement): MutableList<String>
}

data class SeasonalServicePlanAgreementCodeLengthRule(var maxLength: Int) : SeasonalServicePlanAgreementRule {
    override fun check(item: SeasonalServicePlanAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanAgreementRequiredNameRule(
    var message: String,
) : SeasonalServicePlanAgreementRule {
    override fun check(item: SeasonalServicePlanAgreement): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanAgreementRequiredDescriptionRule(
    var message: String,
) : SeasonalServicePlanAgreementRule {
    override fun check(item: SeasonalServicePlanAgreement): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanAgreementNameLengthRule(var maxLength: Int) : SeasonalServicePlanAgreementRule {
    override fun check(item: SeasonalServicePlanAgreement): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanAgreementSegmentLengthRule(
    var maxLength: Int,
) : SeasonalServicePlanAgreementRule {
    override fun check(item: SeasonalServicePlanAgreement): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanAgreementRequiredCodeRule(
    var message: String,
) : SeasonalServicePlanAgreementRule {
    override fun check(item: SeasonalServicePlanAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal service plan agreement. */
data class SeasonalServicePlanAgreementValidator(var rules: MutableList<SeasonalServicePlanAgreementRule>) {
    fun validate(item: SeasonalServicePlanAgreement): MutableList<String> {
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

    fun requireValid(item: SeasonalServicePlanAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalServicePlanAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalServicePlanAgreementValidator(): SeasonalServicePlanAgreementValidator {
    return SeasonalServicePlanAgreementValidator(
        mutableListOf<SeasonalServicePlanAgreementRule>(
            SeasonalServicePlanAgreementCodeLengthRule(120),
            SeasonalServicePlanAgreementRequiredNameRule("seasonal service plan agreement needs this field"),
            SeasonalServicePlanAgreementRequiredDescriptionRule(
                "seasonal service plan agreement needs this field",
            ),
            SeasonalServicePlanAgreementNameLengthRule(120),
            SeasonalServicePlanAgreementSegmentLengthRule(80),
            SeasonalServicePlanAgreementRequiredCodeRule("seasonal service plan agreement needs an entry"),
        ),
    )
}
