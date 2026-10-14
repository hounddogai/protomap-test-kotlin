package com.acme.transport.linehaul.tender.preference

/** A rule a tiered tender milestone must pass. Check returns its problems, or none. */
interface TieredTenderMilestoneRule {
    fun check(item: TieredTenderMilestone): MutableList<String>
}

data class TieredTenderMilestoneRequiredCodeRule(var message: String) : TieredTenderMilestoneRule {
    override fun check(item: TieredTenderMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTenderMilestoneCodeLengthRule(var maxLength: Int) : TieredTenderMilestoneRule {
    override fun check(item: TieredTenderMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTenderMilestoneCodePrefixRule(var prefix: String) : TieredTenderMilestoneRule {
    override fun check(item: TieredTenderMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredTenderMilestoneLabelLengthRule(var maxLength: Int) : TieredTenderMilestoneRule {
    override fun check(item: TieredTenderMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered tender milestone. */
data class TieredTenderMilestoneValidator(var rules: MutableList<TieredTenderMilestoneRule>) {
    fun validate(item: TieredTenderMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredTenderMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTenderMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTenderMilestoneValidator(): TieredTenderMilestoneValidator {
    return TieredTenderMilestoneValidator(
        mutableListOf<TieredTenderMilestoneRule>(
            TieredTenderMilestoneRequiredCodeRule("tiered tender milestone needs this field"),
            TieredTenderMilestoneCodeLengthRule(80),
            TieredTenderMilestoneCodePrefixRule("OPS"),
            TieredTenderMilestoneLabelLengthRule(255),
        ),
    )
}
