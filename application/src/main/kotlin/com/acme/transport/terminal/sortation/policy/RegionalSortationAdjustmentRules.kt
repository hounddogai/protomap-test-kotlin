package com.acme.transport.terminal.sortation.policy

/** A rule a regional sortation adjustment must pass. Check returns its problems, or none. */
interface RegionalSortationAdjustmentRule {
    fun check(item: RegionalSortationAdjustment): MutableList<String>
}

data class RegionalSortationAdjustmentRequiredRouteCodeRule(
    var message: String,
) : RegionalSortationAdjustmentRule {
    override fun check(item: RegionalSortationAdjustment): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalSortationAdjustmentTitleLengthRule(var maxLength: Int) : RegionalSortationAdjustmentRule {
    override fun check(item: RegionalSortationAdjustment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalSortationAdjustmentRouteCodeLengthRule(
    var maxLength: Int,
) : RegionalSortationAdjustmentRule {
    override fun check(item: RegionalSortationAdjustment): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalSortationAdjustmentCodeLengthRule(var maxLength: Int) : RegionalSortationAdjustmentRule {
    override fun check(item: RegionalSortationAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalSortationAdjustmentSummaryLengthRule(var maxLength: Int) : RegionalSortationAdjustmentRule {
    override fun check(item: RegionalSortationAdjustment): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalSortationAdjustmentRequiredSummaryRule(
    var message: String,
) : RegionalSortationAdjustmentRule {
    override fun check(item: RegionalSortationAdjustment): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional sortation adjustment. */
data class RegionalSortationAdjustmentValidator(var rules: MutableList<RegionalSortationAdjustmentRule>) {
    fun validate(item: RegionalSortationAdjustment): MutableList<String> {
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

    fun requireValid(item: RegionalSortationAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalSortationAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalSortationAdjustmentValidator(): RegionalSortationAdjustmentValidator {
    return RegionalSortationAdjustmentValidator(
        mutableListOf<RegionalSortationAdjustmentRule>(
            RegionalSortationAdjustmentRequiredRouteCodeRule("regional sortation adjustment needs an entry"),
            RegionalSortationAdjustmentTitleLengthRule(255),
            RegionalSortationAdjustmentRouteCodeLengthRule(86400),
            RegionalSortationAdjustmentCodeLengthRule(255),
            RegionalSortationAdjustmentSummaryLengthRule(255),
            RegionalSortationAdjustmentRequiredSummaryRule("regional sortation adjustment needs a value"),
        ),
    )
}
