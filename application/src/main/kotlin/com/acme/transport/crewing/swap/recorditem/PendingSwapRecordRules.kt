package com.acme.transport.crewing.swap.recorditem

/** A rule a pending swap record must pass. Check returns its problems, or none. */
interface PendingSwapRecordRule {
    fun check(item: PendingSwapRecord): MutableList<String>
}

data class PendingSwapRecordVisibleGuardRule(var limit: Int) : PendingSwapRecordRule {
    override fun check(item: PendingSwapRecord): MutableList<String> {
        if (item.visible && item.odometer > limit) {
            return mutableListOf<String>("odometer ${item.odometer} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class PendingSwapRecordDistanceKmRangeRule(var min: Double, var max: Double) : PendingSwapRecordRule {
    override fun check(item: PendingSwapRecord): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingSwapRecordRequiredLabelRule(var message: String) : PendingSwapRecordRule {
    override fun check(item: PendingSwapRecord): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending swap record. */
data class PendingSwapRecordValidator(var rules: MutableList<PendingSwapRecordRule>) {
    fun validate(item: PendingSwapRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingSwapRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingSwapRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingSwapRecordValidator(): PendingSwapRecordValidator {
    return PendingSwapRecordValidator(
        mutableListOf<PendingSwapRecordRule>(
            PendingSwapRecordVisibleGuardRule(120),
            PendingSwapRecordDistanceKmRangeRule(2.5, 4474.3),
            PendingSwapRecordRequiredLabelRule("pending swap record needs an entry"),
        ),
    )
}
