package com.acme.transport.network.cutoff.target

/** A rule a regional cutoff target must pass. Check returns its problems, or none. */
interface RegionalCutoffTargetRule {
    fun check(item: RegionalCutoffTarget): MutableList<String>
}

data class RegionalCutoffTargetUnitLengthRule(var maxLength: Int) : RegionalCutoffTargetRule {
    override fun check(item: RegionalCutoffTarget): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCutoffTargetRequiredUnitRule(var message: String) : RegionalCutoffTargetRule {
    override fun check(item: RegionalCutoffTarget): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCutoffTargetCreatedAtRangeRule(var min: Int, var max: Int) : RegionalCutoffTargetRule {
    override fun check(item: RegionalCutoffTarget): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCutoffTargetActiveGuardRule(var limit: Int) : RegionalCutoffTargetRule {
    override fun check(item: RegionalCutoffTarget): MutableList<String> {
        if (item.active && item.createdAt > limit) {
            return mutableListOf<String>("created at ${item.createdAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCutoffTargetCodeLengthRule(var maxLength: Int) : RegionalCutoffTargetRule {
    override fun check(item: RegionalCutoffTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCutoffTargetNameLengthRule(var maxLength: Int) : RegionalCutoffTargetRule {
    override fun check(item: RegionalCutoffTarget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional cutoff target. */
data class RegionalCutoffTargetValidator(var rules: MutableList<RegionalCutoffTargetRule>) {
    fun validate(item: RegionalCutoffTarget): MutableList<String> {
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

    fun requireValid(item: RegionalCutoffTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCutoffTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCutoffTargetValidator(): RegionalCutoffTargetValidator {
    return RegionalCutoffTargetValidator(
        mutableListOf<RegionalCutoffTargetRule>(
            RegionalCutoffTargetUnitLengthRule(3600),
            RegionalCutoffTargetRequiredUnitRule("regional cutoff target needs this field"),
            RegionalCutoffTargetCreatedAtRangeRule(1000, 86400),
            RegionalCutoffTargetActiveGuardRule(32),
            RegionalCutoffTargetCodeLengthRule(1000),
            RegionalCutoffTargetNameLengthRule(64),
        ),
    )
}
