package com.acme.transport.safety.nearmiss.adjustment

/** A rule a near miss adjustment must pass. Check returns its problems, or none. */
interface NearMissAdjustmentRule {
    fun check(item: NearMissAdjustment): MutableList<String>
}

data class NearMissAdjustmentArchivedGuardRule(var limit: Double) : NearMissAdjustmentRule {
    override fun check(item: NearMissAdjustment): MutableList<String> {
        if (item.archived && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class NearMissAdjustmentAmountRangeRule(var min: Double, var max: Double) : NearMissAdjustmentRule {
    override fun check(item: NearMissAdjustment): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NearMissAdjustmentCodeLengthRule(var maxLength: Int) : NearMissAdjustmentRule {
    override fun check(item: NearMissAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissAdjustmentTitleLengthRule(var maxLength: Int) : NearMissAdjustmentRule {
    override fun check(item: NearMissAdjustment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissAdjustmentRequiredCodeRule(var message: String) : NearMissAdjustmentRule {
    override fun check(item: NearMissAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissAdjustmentRequiredTitleRule(var message: String) : NearMissAdjustmentRule {
    override fun check(item: NearMissAdjustment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss adjustment. */
data class NearMissAdjustmentValidator(var rules: MutableList<NearMissAdjustmentRule>) {
    fun validate(item: NearMissAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NearMissAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissAdjustmentValidator(): NearMissAdjustmentValidator {
    return NearMissAdjustmentValidator(
        mutableListOf<NearMissAdjustmentRule>(
            NearMissAdjustmentArchivedGuardRule(1.8),
            NearMissAdjustmentAmountRangeRule(7.0, 2130.0),
            NearMissAdjustmentCodeLengthRule(64),
            NearMissAdjustmentTitleLengthRule(120),
            NearMissAdjustmentRequiredCodeRule("near miss adjustment needs this field"),
            NearMissAdjustmentRequiredTitleRule("near miss adjustment needs an entry"),
        ),
    )
}
