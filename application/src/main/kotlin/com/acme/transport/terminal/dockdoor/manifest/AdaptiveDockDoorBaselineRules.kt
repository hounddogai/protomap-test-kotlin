package com.acme.transport.terminal.dockdoor.manifest

/** A rule a adaptive dock door baseline must pass. Check returns its problems, or none. */
interface AdaptiveDockDoorBaselineRule {
    fun check(item: AdaptiveDockDoorBaseline): MutableList<String>
}

data class AdaptiveDockDoorBaselineRequiredTitleRule(var message: String) : AdaptiveDockDoorBaselineRule {
    override fun check(item: AdaptiveDockDoorBaseline): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDockDoorBaselineTitleLengthRule(var maxLength: Int) : AdaptiveDockDoorBaselineRule {
    override fun check(item: AdaptiveDockDoorBaseline): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDockDoorBaselineRequiredDepotRule(var message: String) : AdaptiveDockDoorBaselineRule {
    override fun check(item: AdaptiveDockDoorBaseline): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive dock door baseline. */
data class AdaptiveDockDoorBaselineValidator(var rules: MutableList<AdaptiveDockDoorBaselineRule>) {
    fun validate(item: AdaptiveDockDoorBaseline): MutableList<String> {
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

    fun requireValid(item: AdaptiveDockDoorBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDockDoorBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDockDoorBaselineValidator(): AdaptiveDockDoorBaselineValidator {
    return AdaptiveDockDoorBaselineValidator(
        mutableListOf<AdaptiveDockDoorBaselineRule>(
            AdaptiveDockDoorBaselineRequiredTitleRule("adaptive dock door baseline needs an entry"),
            AdaptiveDockDoorBaselineTitleLengthRule(80),
            AdaptiveDockDoorBaselineRequiredDepotRule("adaptive dock door baseline needs an entry"),
        ),
    )
}
