package com.acme.transport.lastmile.redelivery.quota

/** A rule a redelivery snapshot must pass. Check returns its problems, or none. */
interface RedeliverySnapshotRule {
    fun check(item: RedeliverySnapshot): MutableList<String>
}

data class RedeliverySnapshotChannelLengthRule(var maxLength: Int) : RedeliverySnapshotRule {
    override fun check(item: RedeliverySnapshot): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliverySnapshotRequiredCodeRule(var message: String) : RedeliverySnapshotRule {
    override fun check(item: RedeliverySnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliverySnapshotRequiredLabelRule(var message: String) : RedeliverySnapshotRule {
    override fun check(item: RedeliverySnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliverySnapshotLabelLengthRule(var maxLength: Int) : RedeliverySnapshotRule {
    override fun check(item: RedeliverySnapshot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliverySnapshotTitleLengthRule(var maxLength: Int) : RedeliverySnapshotRule {
    override fun check(item: RedeliverySnapshot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliverySnapshotRequiredTitleRule(var message: String) : RedeliverySnapshotRule {
    override fun check(item: RedeliverySnapshot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery snapshot. */
data class RedeliverySnapshotValidator(var rules: MutableList<RedeliverySnapshotRule>) {
    fun validate(item: RedeliverySnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliverySnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliverySnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliverySnapshotValidator(): RedeliverySnapshotValidator {
    return RedeliverySnapshotValidator(
        mutableListOf<RedeliverySnapshotRule>(
            RedeliverySnapshotChannelLengthRule(16),
            RedeliverySnapshotRequiredCodeRule("redelivery snapshot needs a value"),
            RedeliverySnapshotRequiredLabelRule("redelivery snapshot needs a value"),
            RedeliverySnapshotLabelLengthRule(120),
            RedeliverySnapshotTitleLengthRule(1000),
            RedeliverySnapshotRequiredTitleRule("redelivery snapshot needs this field"),
        ),
    )
}
