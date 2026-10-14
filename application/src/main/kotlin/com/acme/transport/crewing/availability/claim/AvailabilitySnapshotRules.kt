package com.acme.transport.crewing.availability.claim

/** A rule a availability snapshot must pass. Check returns its problems, or none. */
interface AvailabilitySnapshotRule {
    fun check(item: AvailabilitySnapshot): MutableList<String>
}

data class AvailabilitySnapshotCurrencyLengthRule(var maxLength: Int) : AvailabilitySnapshotRule {
    override fun check(item: AvailabilitySnapshot): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilitySnapshotRequiredCurrencyRule(var message: String) : AvailabilitySnapshotRule {
    override fun check(item: AvailabilitySnapshot): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilitySnapshotRequiredCodeRule(var message: String) : AvailabilitySnapshotRule {
    override fun check(item: AvailabilitySnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability snapshot. */
data class AvailabilitySnapshotValidator(var rules: MutableList<AvailabilitySnapshotRule>) {
    fun validate(item: AvailabilitySnapshot): MutableList<String> {
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

    fun requireValid(item: AvailabilitySnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilitySnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilitySnapshotValidator(): AvailabilitySnapshotValidator {
    return AvailabilitySnapshotValidator(
        mutableListOf<AvailabilitySnapshotRule>(
            AvailabilitySnapshotCurrencyLengthRule(3600),
            AvailabilitySnapshotRequiredCurrencyRule("availability snapshot needs an entry"),
            AvailabilitySnapshotRequiredCodeRule("availability snapshot needs a value"),
        ),
    )
}
