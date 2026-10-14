package com.acme.transport.customs.tariff.draft

/** A rule a tariff milestone must pass. Check returns its problems, or none. */
interface TariffMilestoneRule {
    fun check(item: TariffMilestone): MutableList<String>
}

data class TariffMilestoneNameLengthRule(var maxLength: Int) : TariffMilestoneRule {
    override fun check(item: TariffMilestone): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffMilestoneOwnerIdLengthRule(var maxLength: Int) : TariffMilestoneRule {
    override fun check(item: TariffMilestone): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffMilestoneRequiredOwnerIdRule(var message: String) : TariffMilestoneRule {
    override fun check(item: TariffMilestone): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffMilestoneRequiredCodeRule(var message: String) : TariffMilestoneRule {
    override fun check(item: TariffMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffMilestoneCeilingRangeRule(var min: Double, var max: Double) : TariffMilestoneRule {
    override fun check(item: TariffMilestone): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff milestone. */
data class TariffMilestoneValidator(var rules: MutableList<TariffMilestoneRule>) {
    fun validate(item: TariffMilestone): MutableList<String> {
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

    fun requireValid(item: TariffMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffMilestoneValidator(): TariffMilestoneValidator {
    return TariffMilestoneValidator(
        mutableListOf<TariffMilestoneRule>(
            TariffMilestoneNameLengthRule(16),
            TariffMilestoneOwnerIdLengthRule(86400),
            TariffMilestoneRequiredOwnerIdRule("tariff milestone needs a value"),
            TariffMilestoneRequiredCodeRule("tariff milestone needs an entry"),
            TariffMilestoneCeilingRangeRule(2.2, 1146.4),
        ),
    )
}
