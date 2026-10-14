package com.acme.transport.fleetops.defect.agreement

/** A rule a manual defect agreement must pass. Check returns its problems, or none. */
interface ManualDefectAgreementRule {
    fun check(item: ManualDefectAgreement): MutableList<String>
}

data class ManualDefectAgreementActiveGuardRule(var limit: Int) : ManualDefectAgreementRule {
    override fun check(item: ManualDefectAgreement): MutableList<String> {
        if (item.active && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class ManualDefectAgreementLocaleLengthRule(var maxLength: Int) : ManualDefectAgreementRule {
    override fun check(item: ManualDefectAgreement): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDefectAgreementRequiredTitleRule(var message: String) : ManualDefectAgreementRule {
    override fun check(item: ManualDefectAgreement): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual defect agreement. */
data class ManualDefectAgreementValidator(var rules: MutableList<ManualDefectAgreementRule>) {
    fun validate(item: ManualDefectAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualDefectAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDefectAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDefectAgreementValidator(): ManualDefectAgreementValidator {
    return ManualDefectAgreementValidator(
        mutableListOf<ManualDefectAgreementRule>(
            ManualDefectAgreementActiveGuardRule(3600),
            ManualDefectAgreementLocaleLengthRule(3600),
            ManualDefectAgreementRequiredTitleRule("manual defect agreement needs an entry"),
        ),
    )
}
