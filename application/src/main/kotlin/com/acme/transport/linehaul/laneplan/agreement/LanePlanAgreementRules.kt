package com.acme.transport.linehaul.laneplan.agreement

import com.acme.transport.kit.text.normalize

/** A rule a lane plan agreement must pass. Check returns its problems, or none. */
interface LanePlanAgreementRule {
    fun check(item: LanePlanAgreement): MutableList<String>
}

data class LanePlanAgreementCodeLengthRule(var maxLength: Int) : LanePlanAgreementRule {
    override fun check(item: LanePlanAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanAgreementThresholdRangeRule(var min: Double, var max: Double) : LanePlanAgreementRule {
    override fun check(item: LanePlanAgreement): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanAgreementAllowedCodeRule(var allowed: MutableList<String>) : LanePlanAgreementRule {
    override fun check(item: LanePlanAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanAgreementArchivedGuardRule(var limit: Double) : LanePlanAgreementRule {
    override fun check(item: LanePlanAgreement): MutableList<String> {
        if (item.archived && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanAgreementNameLengthRule(var maxLength: Int) : LanePlanAgreementRule {
    override fun check(item: LanePlanAgreement): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan agreement. */
data class LanePlanAgreementValidator(var rules: MutableList<LanePlanAgreementRule>) {
    fun validate(item: LanePlanAgreement): MutableList<String> {
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

    fun requireValid(item: LanePlanAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanAgreementValidator(): LanePlanAgreementValidator {
    return LanePlanAgreementValidator(
        mutableListOf<LanePlanAgreementRule>(
            LanePlanAgreementCodeLengthRule(1000),
            LanePlanAgreementThresholdRangeRule(5.3, 2356.0),
            LanePlanAgreementAllowedCodeRule(mutableListOf<String>("scheduled", "lenient", "regional")),
            LanePlanAgreementArchivedGuardRule(3.1),
            LanePlanAgreementNameLengthRule(3600),
        ),
    )
}
