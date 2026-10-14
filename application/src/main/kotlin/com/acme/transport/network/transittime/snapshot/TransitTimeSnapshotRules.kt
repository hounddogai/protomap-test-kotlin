package com.acme.transport.network.transittime.snapshot

/** A rule a transit time snapshot must pass. Check returns its problems, or none. */
interface TransitTimeSnapshotRule {
    fun check(item: TransitTimeSnapshot): MutableList<String>
}

data class TransitTimeSnapshotWeightRangeRule(var min: Double, var max: Double) : TransitTimeSnapshotRule {
    override fun check(item: TransitTimeSnapshot): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeSnapshotRequiredDepotRule(var message: String) : TransitTimeSnapshotRule {
    override fun check(item: TransitTimeSnapshot): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeSnapshotMaximumRangeRule(var min: Double, var max: Double) : TransitTimeSnapshotRule {
    override fun check(item: TransitTimeSnapshot): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a transit time snapshot. */
data class TransitTimeSnapshotValidator(var rules: MutableList<TransitTimeSnapshotRule>) {
    fun validate(item: TransitTimeSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeSnapshotValidator(): TransitTimeSnapshotValidator {
    return TransitTimeSnapshotValidator(
        mutableListOf<TransitTimeSnapshotRule>(
            TransitTimeSnapshotWeightRangeRule(9.4, 572.0),
            TransitTimeSnapshotRequiredDepotRule("transit time snapshot needs an entry"),
            TransitTimeSnapshotMaximumRangeRule(3.8, 2848.5),
        ),
    )
}
