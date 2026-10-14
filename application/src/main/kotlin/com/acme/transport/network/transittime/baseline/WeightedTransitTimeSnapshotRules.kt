package com.acme.transport.network.transittime.baseline

/** A rule a weighted transit time snapshot must pass. Check returns its problems, or none. */
interface WeightedTransitTimeSnapshotRule {
    fun check(item: WeightedTransitTimeSnapshot): MutableList<String>
}

data class WeightedTransitTimeSnapshotRequiredLabelRule(var message: String) : WeightedTransitTimeSnapshotRule {
    override fun check(item: WeightedTransitTimeSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedTransitTimeSnapshotRouteCodeLengthRule(
    var maxLength: Int,
) : WeightedTransitTimeSnapshotRule {
    override fun check(item: WeightedTransitTimeSnapshot): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTransitTimeSnapshotRequiredTrailerIdRule(
    var message: String,
) : WeightedTransitTimeSnapshotRule {
    override fun check(item: WeightedTransitTimeSnapshot): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted transit time snapshot. */
data class WeightedTransitTimeSnapshotValidator(var rules: MutableList<WeightedTransitTimeSnapshotRule>) {
    fun validate(item: WeightedTransitTimeSnapshot): MutableList<String> {
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

    fun requireValid(item: WeightedTransitTimeSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTransitTimeSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTransitTimeSnapshotValidator(): WeightedTransitTimeSnapshotValidator {
    return WeightedTransitTimeSnapshotValidator(
        mutableListOf<WeightedTransitTimeSnapshotRule>(
            WeightedTransitTimeSnapshotRequiredLabelRule("weighted transit time snapshot needs an entry"),
            WeightedTransitTimeSnapshotRouteCodeLengthRule(86400),
            WeightedTransitTimeSnapshotRequiredTrailerIdRule("weighted transit time snapshot needs this field"),
        ),
    )
}
