package com.acme.transport.linehaul.tender.milestone

/** A rule a tender milestone must pass. Check returns its problems, or none. */
interface TenderMilestoneRule {
    fun check(item: TenderMilestone): MutableList<String>
}

data class TenderMilestonePalletsRangeRule(var min: Int, var max: Int) : TenderMilestoneRule {
    override fun check(item: TenderMilestone): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TenderMilestoneChannelLengthRule(var maxLength: Int) : TenderMilestoneRule {
    override fun check(item: TenderMilestone): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderMilestoneCodePrefixRule(var prefix: String) : TenderMilestoneRule {
    override fun check(item: TenderMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TenderMilestoneRequiredCodeRule(var message: String) : TenderMilestoneRule {
    override fun check(item: TenderMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender milestone. */
data class TenderMilestoneValidator(var rules: MutableList<TenderMilestoneRule>) {
    fun validate(item: TenderMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderMilestoneValidator(): TenderMilestoneValidator {
    return TenderMilestoneValidator(
        mutableListOf<TenderMilestoneRule>(
            TenderMilestonePalletsRangeRule(80, 120),
            TenderMilestoneChannelLengthRule(32),
            TenderMilestoneCodePrefixRule("OPS"),
            TenderMilestoneRequiredCodeRule("tender milestone needs this field"),
        ),
    )
}
