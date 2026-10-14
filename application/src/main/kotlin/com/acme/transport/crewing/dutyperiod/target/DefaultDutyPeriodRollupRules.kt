package com.acme.transport.crewing.dutyperiod.target

/** A rule a default duty period rollup must pass. Check returns its problems, or none. */
interface DefaultDutyPeriodRollupRule {
    fun check(item: DefaultDutyPeriodRollup): MutableList<String>
}

data class DefaultDutyPeriodRollupVisibleGuardRule(var limit: Double) : DefaultDutyPeriodRollupRule {
    override fun check(item: DefaultDutyPeriodRollup): MutableList<String> {
        if (item.visible && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDutyPeriodRollupCodeLengthRule(var maxLength: Int) : DefaultDutyPeriodRollupRule {
    override fun check(item: DefaultDutyPeriodRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDutyPeriodRollupDepotLengthRule(var maxLength: Int) : DefaultDutyPeriodRollupRule {
    override fun check(item: DefaultDutyPeriodRollup): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDutyPeriodRollupTitleLengthRule(var maxLength: Int) : DefaultDutyPeriodRollupRule {
    override fun check(item: DefaultDutyPeriodRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDutyPeriodRollupNameLengthRule(var maxLength: Int) : DefaultDutyPeriodRollupRule {
    override fun check(item: DefaultDutyPeriodRollup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDutyPeriodRollupRequiredTitleRule(var message: String) : DefaultDutyPeriodRollupRule {
    override fun check(item: DefaultDutyPeriodRollup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default duty period rollup. */
data class DefaultDutyPeriodRollupValidator(var rules: MutableList<DefaultDutyPeriodRollupRule>) {
    fun validate(item: DefaultDutyPeriodRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultDutyPeriodRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDutyPeriodRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDutyPeriodRollupValidator(): DefaultDutyPeriodRollupValidator {
    return DefaultDutyPeriodRollupValidator(
        mutableListOf<DefaultDutyPeriodRollupRule>(
            DefaultDutyPeriodRollupVisibleGuardRule(5.2),
            DefaultDutyPeriodRollupCodeLengthRule(80),
            DefaultDutyPeriodRollupDepotLengthRule(32),
            DefaultDutyPeriodRollupTitleLengthRule(255),
            DefaultDutyPeriodRollupNameLengthRule(64),
            DefaultDutyPeriodRollupRequiredTitleRule("default duty period rollup needs this field"),
        ),
    )
}
