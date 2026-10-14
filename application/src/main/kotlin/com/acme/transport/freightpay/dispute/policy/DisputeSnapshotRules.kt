package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.kit.text.normalize

/** A rule a dispute snapshot must pass. Check returns its problems, or none. */
interface DisputeSnapshotRule {
    fun check(item: DisputeSnapshot): MutableList<String>
}

data class DisputeSnapshotAllowedCodeRule(var allowed: MutableList<String>) : DisputeSnapshotRule {
    override fun check(item: DisputeSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class DisputeSnapshotRequiredTitleRule(var message: String) : DisputeSnapshotRule {
    override fun check(item: DisputeSnapshot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeSnapshotRequiredCodeRule(var message: String) : DisputeSnapshotRule {
    override fun check(item: DisputeSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeSnapshotThresholdRangeRule(var min: Double, var max: Double) : DisputeSnapshotRule {
    override fun check(item: DisputeSnapshot): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DisputeSnapshotGraceDaysRangeRule(var min: Int, var max: Int) : DisputeSnapshotRule {
    override fun check(item: DisputeSnapshot): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute snapshot. */
data class DisputeSnapshotValidator(var rules: MutableList<DisputeSnapshotRule>) {
    fun validate(item: DisputeSnapshot): MutableList<String> {
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

    fun requireValid(item: DisputeSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeSnapshotValidator(): DisputeSnapshotValidator {
    return DisputeSnapshotValidator(
        mutableListOf<DisputeSnapshotRule>(
            DisputeSnapshotAllowedCodeRule(mutableListOf<String>()),
            DisputeSnapshotRequiredTitleRule("dispute snapshot needs an entry"),
            DisputeSnapshotRequiredCodeRule("dispute snapshot needs a value"),
            DisputeSnapshotThresholdRangeRule(3.1, 605.3),
            DisputeSnapshotGraceDaysRangeRule(16, 32),
        ),
    )
}
