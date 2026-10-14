package com.acme.transport.safety.nearmiss.ledger

/** A rule a near miss snapshot must pass. Check returns its problems, or none. */
interface NearMissSnapshotRule {
    fun check(item: NearMissSnapshot): MutableList<String>
}

data class NearMissSnapshotRequiredTitleRule(var message: String) : NearMissSnapshotRule {
    override fun check(item: NearMissSnapshot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissSnapshotRequiredCodeRule(var message: String) : NearMissSnapshotRule {
    override fun check(item: NearMissSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissSnapshotRequiredChannelRule(var message: String) : NearMissSnapshotRule {
    override fun check(item: NearMissSnapshot): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss snapshot. */
data class NearMissSnapshotValidator(var rules: MutableList<NearMissSnapshotRule>) {
    fun validate(item: NearMissSnapshot): MutableList<String> {
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

    fun requireValid(item: NearMissSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissSnapshotValidator(): NearMissSnapshotValidator {
    return NearMissSnapshotValidator(
        mutableListOf<NearMissSnapshotRule>(
            NearMissSnapshotRequiredTitleRule("near miss snapshot needs a value"),
            NearMissSnapshotRequiredCodeRule("near miss snapshot needs a value"),
            NearMissSnapshotRequiredChannelRule("near miss snapshot needs a value"),
        ),
    )
}
