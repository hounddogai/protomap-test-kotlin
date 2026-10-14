package com.acme.transport.freightpay.accessorial.accessorialcycle

/** A rule a regional accessorial cycle must pass. Check returns its problems, or none. */
interface RegionalAccessorialCycleRule {
    fun check(item: RegionalAccessorialCycle): MutableList<String>
}

data class RegionalAccessorialCycleApprovedGuardRule(var limit: Double) : RegionalAccessorialCycleRule {
    override fun check(item: RegionalAccessorialCycle): MutableList<String> {
        if (item.approved && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAccessorialCycleRequiredTitleRule(var message: String) : RegionalAccessorialCycleRule {
    override fun check(item: RegionalAccessorialCycle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalAccessorialCycleCurrencyLengthRule(var maxLength: Int) : RegionalAccessorialCycleRule {
    override fun check(item: RegionalAccessorialCycle): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAccessorialCycleTitleLengthRule(var maxLength: Int) : RegionalAccessorialCycleRule {
    override fun check(item: RegionalAccessorialCycle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAccessorialCycleCodeLengthRule(var maxLength: Int) : RegionalAccessorialCycleRule {
    override fun check(item: RegionalAccessorialCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAccessorialCycleRequiredLaneRule(var message: String) : RegionalAccessorialCycleRule {
    override fun check(item: RegionalAccessorialCycle): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional accessorial cycle. */
data class RegionalAccessorialCycleValidator(var rules: MutableList<RegionalAccessorialCycleRule>) {
    fun validate(item: RegionalAccessorialCycle): MutableList<String> {
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

    fun requireValid(item: RegionalAccessorialCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalAccessorialCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalAccessorialCycleValidator(): RegionalAccessorialCycleValidator {
    return RegionalAccessorialCycleValidator(
        mutableListOf<RegionalAccessorialCycleRule>(
            RegionalAccessorialCycleApprovedGuardRule(7.4),
            RegionalAccessorialCycleRequiredTitleRule("regional accessorial cycle needs an entry"),
            RegionalAccessorialCycleCurrencyLengthRule(1000),
            RegionalAccessorialCycleTitleLengthRule(1000),
            RegionalAccessorialCycleCodeLengthRule(64),
            RegionalAccessorialCycleRequiredLaneRule("regional accessorial cycle needs a value"),
        ),
    )
}
