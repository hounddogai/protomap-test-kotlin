package com.acme.transport.freightpay.dispute.disputeassignment

/** A rule a capped dispute group must pass. Check returns its problems, or none. */
interface CappedDisputeGroupRule {
    fun check(item: CappedDisputeGroup): MutableList<String>
}

data class CappedDisputeGroupScoreRangeRule(var min: Double, var max: Double) : CappedDisputeGroupRule {
    override fun check(item: CappedDisputeGroup): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class CappedDisputeGroupReviewerLengthRule(var maxLength: Int) : CappedDisputeGroupRule {
    override fun check(item: CappedDisputeGroup): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDisputeGroupDriverIdLengthRule(var maxLength: Int) : CappedDisputeGroupRule {
    override fun check(item: CappedDisputeGroup): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDisputeGroupTitleLengthRule(var maxLength: Int) : CappedDisputeGroupRule {
    override fun check(item: CappedDisputeGroup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDisputeGroupRequiredTitleRule(var message: String) : CappedDisputeGroupRule {
    override fun check(item: CappedDisputeGroup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDisputeGroupRequiredCodeRule(var message: String) : CappedDisputeGroupRule {
    override fun check(item: CappedDisputeGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped dispute group. */
data class CappedDisputeGroupValidator(var rules: MutableList<CappedDisputeGroupRule>) {
    fun validate(item: CappedDisputeGroup): MutableList<String> {
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

    fun requireValid(item: CappedDisputeGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDisputeGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDisputeGroupValidator(): CappedDisputeGroupValidator {
    return CappedDisputeGroupValidator(
        mutableListOf<CappedDisputeGroupRule>(
            CappedDisputeGroupScoreRangeRule(8.0, 3899.9),
            CappedDisputeGroupReviewerLengthRule(1000),
            CappedDisputeGroupDriverIdLengthRule(3600),
            CappedDisputeGroupTitleLengthRule(1000),
            CappedDisputeGroupRequiredTitleRule("capped dispute group needs this field"),
            CappedDisputeGroupRequiredCodeRule("capped dispute group needs a value"),
        ),
    )
}
