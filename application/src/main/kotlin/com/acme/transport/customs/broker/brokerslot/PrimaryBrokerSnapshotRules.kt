package com.acme.transport.customs.broker.brokerslot

import com.acme.transport.kit.text.normalize

/** A rule a primary broker snapshot must pass. Check returns its problems, or none. */
interface PrimaryBrokerSnapshotRule {
    fun check(item: PrimaryBrokerSnapshot): MutableList<String>
}

data class PrimaryBrokerSnapshotLabelLengthRule(var maxLength: Int) : PrimaryBrokerSnapshotRule {
    override fun check(item: PrimaryBrokerSnapshot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBrokerSnapshotAllowedSegmentRule(
    var allowed: MutableList<String>,
) : PrimaryBrokerSnapshotRule {
    override fun check(item: PrimaryBrokerSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBrokerSnapshotTimezoneLengthRule(var maxLength: Int) : PrimaryBrokerSnapshotRule {
    override fun check(item: PrimaryBrokerSnapshot): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary broker snapshot. */
data class PrimaryBrokerSnapshotValidator(var rules: MutableList<PrimaryBrokerSnapshotRule>) {
    fun validate(item: PrimaryBrokerSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryBrokerSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryBrokerSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryBrokerSnapshotValidator(): PrimaryBrokerSnapshotValidator {
    return PrimaryBrokerSnapshotValidator(
        mutableListOf<PrimaryBrokerSnapshotRule>(
            PrimaryBrokerSnapshotLabelLengthRule(80),
            PrimaryBrokerSnapshotAllowedSegmentRule(mutableListOf<String>()),
            PrimaryBrokerSnapshotTimezoneLengthRule(86400),
        ),
    )
}
