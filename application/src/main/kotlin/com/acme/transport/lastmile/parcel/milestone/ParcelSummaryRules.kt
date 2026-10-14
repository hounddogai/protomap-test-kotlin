package com.acme.transport.lastmile.parcel.milestone

/** A rule a parcel summary must pass. Check returns its problems, or none. */
interface ParcelSummaryRule {
    fun check(item: ParcelSummary): MutableList<String>
}

data class ParcelSummaryRequiredTitleRule(var message: String) : ParcelSummaryRule {
    override fun check(item: ParcelSummary): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelSummaryLoadKgRangeRule(var min: Double, var max: Double) : ParcelSummaryRule {
    override fun check(item: ParcelSummary): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class ParcelSummaryRequiredShiftCodeRule(var message: String) : ParcelSummaryRule {
    override fun check(item: ParcelSummary): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel summary. */
data class ParcelSummaryValidator(var rules: MutableList<ParcelSummaryRule>) {
    fun validate(item: ParcelSummary): MutableList<String> {
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

    fun requireValid(item: ParcelSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelSummaryValidator(): ParcelSummaryValidator {
    return ParcelSummaryValidator(
        mutableListOf<ParcelSummaryRule>(
            ParcelSummaryRequiredTitleRule("parcel summary needs an entry"),
            ParcelSummaryLoadKgRangeRule(4.5, 2500.3),
            ParcelSummaryRequiredShiftCodeRule("parcel summary needs an entry"),
        ),
    )
}
