package com.acme.transport.crewing.swap.score

/** A rule a swap snapshot must pass. Check returns its problems, or none. */
interface SwapSnapshotRule {
    fun check(item: SwapSnapshot): MutableList<String>
}

data class SwapSnapshotCountryLengthRule(var maxLength: Int) : SwapSnapshotRule {
    override fun check(item: SwapSnapshot): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapSnapshotRequiredCodeRule(var message: String) : SwapSnapshotRule {
    override fun check(item: SwapSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapSnapshotRequiredNameRule(var message: String) : SwapSnapshotRule {
    override fun check(item: SwapSnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapSnapshotAmountRangeRule(var min: Double, var max: Double) : SwapSnapshotRule {
    override fun check(item: SwapSnapshot): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SwapSnapshotNameLengthRule(var maxLength: Int) : SwapSnapshotRule {
    override fun check(item: SwapSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapSnapshotRequiredCountryRule(var message: String) : SwapSnapshotRule {
    override fun check(item: SwapSnapshot): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap snapshot. */
data class SwapSnapshotValidator(var rules: MutableList<SwapSnapshotRule>) {
    fun validate(item: SwapSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SwapSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapSnapshotValidator(): SwapSnapshotValidator {
    return SwapSnapshotValidator(
        mutableListOf<SwapSnapshotRule>(
            SwapSnapshotCountryLengthRule(80),
            SwapSnapshotRequiredCodeRule("swap snapshot needs this field"),
            SwapSnapshotRequiredNameRule("swap snapshot needs an entry"),
            SwapSnapshotAmountRangeRule(3.3, 4036.7),
            SwapSnapshotNameLengthRule(16),
            SwapSnapshotRequiredCountryRule("swap snapshot needs an entry"),
        ),
    )
}
