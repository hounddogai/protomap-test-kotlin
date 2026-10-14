package com.acme.transport.fleetops.tire.tireagreement

/** A rule a pending tire agreement must pass. Check returns its problems, or none. */
interface PendingTireAgreementRule {
    fun check(item: PendingTireAgreement): MutableList<String>
}

data class PendingTireAgreementCodeLengthRule(var maxLength: Int) : PendingTireAgreementRule {
    override fun check(item: PendingTireAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTireAgreementRequiredLaneRule(var message: String) : PendingTireAgreementRule {
    override fun check(item: PendingTireAgreement): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTireAgreementLaneLengthRule(var maxLength: Int) : PendingTireAgreementRule {
    override fun check(item: PendingTireAgreement): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending tire agreement. */
data class PendingTireAgreementValidator(var rules: MutableList<PendingTireAgreementRule>) {
    fun validate(item: PendingTireAgreement): MutableList<String> {
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

    fun requireValid(item: PendingTireAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTireAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTireAgreementValidator(): PendingTireAgreementValidator {
    return PendingTireAgreementValidator(
        mutableListOf<PendingTireAgreementRule>(
            PendingTireAgreementCodeLengthRule(120),
            PendingTireAgreementRequiredLaneRule("pending tire agreement needs an entry"),
            PendingTireAgreementLaneLengthRule(64),
        ),
    )
}
