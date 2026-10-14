package com.acme.transport.lastmile.redelivery.metric

import com.acme.transport.kit.text.normalize

/** A rule a default redelivery snapshot must pass. Check returns its problems, or none. */
interface DefaultRedeliverySnapshotRule {
    fun check(item: DefaultRedeliverySnapshot): MutableList<String>
}

data class DefaultRedeliverySnapshotApprovedGuardRule(var limit: Int) : DefaultRedeliverySnapshotRule {
    override fun check(item: DefaultRedeliverySnapshot): MutableList<String> {
        if (item.approved && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRedeliverySnapshotRequiredCodeRule(var message: String) : DefaultRedeliverySnapshotRule {
    override fun check(item: DefaultRedeliverySnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRedeliverySnapshotAllowedDepotRule(
    var allowed: MutableList<String>,
) : DefaultRedeliverySnapshotRule {
    override fun check(item: DefaultRedeliverySnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRedeliverySnapshotStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : DefaultRedeliverySnapshotRule {
    override fun check(item: DefaultRedeliverySnapshot): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRedeliverySnapshotCodeLengthRule(var maxLength: Int) : DefaultRedeliverySnapshotRule {
    override fun check(item: DefaultRedeliverySnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default redelivery snapshot. */
data class DefaultRedeliverySnapshotValidator(var rules: MutableList<DefaultRedeliverySnapshotRule>) {
    fun validate(item: DefaultRedeliverySnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultRedeliverySnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRedeliverySnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRedeliverySnapshotValidator(): DefaultRedeliverySnapshotValidator {
    return DefaultRedeliverySnapshotValidator(
        mutableListOf<DefaultRedeliverySnapshotRule>(
            DefaultRedeliverySnapshotApprovedGuardRule(3600),
            DefaultRedeliverySnapshotRequiredCodeRule("default redelivery snapshot needs this field"),
            DefaultRedeliverySnapshotAllowedDepotRule(mutableListOf<String>("legacy", "regional", "batched")),
            DefaultRedeliverySnapshotStartsAtRangeRule(255, 1000),
            DefaultRedeliverySnapshotCodeLengthRule(120),
        ),
    )
}
