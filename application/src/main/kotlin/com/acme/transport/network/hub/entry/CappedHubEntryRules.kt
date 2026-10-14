package com.acme.transport.network.hub.entry

/** A rule a capped hub entry must pass. Check returns its problems, or none. */
interface CappedHubEntryRule {
    fun check(item: CappedHubEntry): MutableList<String>
}

data class CappedHubEntryExternalRefLengthRule(var maxLength: Int) : CappedHubEntryRule {
    override fun check(item: CappedHubEntry): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedHubEntryRatioRangeRule(var min: Double, var max: Double) : CappedHubEntryRule {
    override fun check(item: CappedHubEntry): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedHubEntryFloorValueRangeRule(var min: Double, var max: Double) : CappedHubEntryRule {
    override fun check(item: CappedHubEntry): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped hub entry. */
data class CappedHubEntryValidator(var rules: MutableList<CappedHubEntryRule>) {
    fun validate(item: CappedHubEntry): MutableList<String> {
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

    fun requireValid(item: CappedHubEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedHubEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedHubEntryValidator(): CappedHubEntryValidator {
    return CappedHubEntryValidator(
        mutableListOf<CappedHubEntryRule>(
            CappedHubEntryExternalRefLengthRule(32),
            CappedHubEntryRatioRangeRule(8.6, 4621.6),
            CappedHubEntryFloorValueRangeRule(8.8, 4429.6),
        ),
    )
}
