package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.kit.text.normalize

/** A rule a batched duty period agreement must pass. Check returns its problems, or none. */
interface BatchedDutyPeriodAgreementRule {
    fun check(item: BatchedDutyPeriodAgreement): MutableList<String>
}

data class BatchedDutyPeriodAgreementPinnedGuardRule(var limit: Double) : BatchedDutyPeriodAgreementRule {
    override fun check(item: BatchedDutyPeriodAgreement): MutableList<String> {
        if (item.pinned && item.budget > limit) {
            return mutableListOf<String>("budget ${item.budget} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDutyPeriodAgreementAllowedTitleRule(
    var allowed: MutableList<String>,
) : BatchedDutyPeriodAgreementRule {
    override fun check(item: BatchedDutyPeriodAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDutyPeriodAgreementRequiredCodeRule(var message: String) : BatchedDutyPeriodAgreementRule {
    override fun check(item: BatchedDutyPeriodAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDutyPeriodAgreementRequiredTitleRule(var message: String) : BatchedDutyPeriodAgreementRule {
    override fun check(item: BatchedDutyPeriodAgreement): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched duty period agreement. */
data class BatchedDutyPeriodAgreementValidator(var rules: MutableList<BatchedDutyPeriodAgreementRule>) {
    fun validate(item: BatchedDutyPeriodAgreement): MutableList<String> {
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

    fun requireValid(item: BatchedDutyPeriodAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDutyPeriodAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDutyPeriodAgreementValidator(): BatchedDutyPeriodAgreementValidator {
    return BatchedDutyPeriodAgreementValidator(
        mutableListOf<BatchedDutyPeriodAgreementRule>(
            BatchedDutyPeriodAgreementPinnedGuardRule(0.6),
            BatchedDutyPeriodAgreementAllowedTitleRule(mutableListOf<String>("primary", "global")),
            BatchedDutyPeriodAgreementRequiredCodeRule("batched duty period agreement needs this field"),
            BatchedDutyPeriodAgreementRequiredTitleRule("batched duty period agreement needs a value"),
        ),
    )
}
