package com.acme.transport.linehaul.tender.plan

/** A rule a tender group must pass. Check returns its problems, or none. */
interface TenderGroupRule {
    fun check(item: TenderGroup): MutableList<String>
}

data class TenderGroupCeilingRangeRule(var min: Double, var max: Double) : TenderGroupRule {
    override fun check(item: TenderGroup): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TenderGroupChannelLengthRule(var maxLength: Int) : TenderGroupRule {
    override fun check(item: TenderGroup): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderGroupRequiredChannelRule(var message: String) : TenderGroupRule {
    override fun check(item: TenderGroup): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderGroupCodeLengthRule(var maxLength: Int) : TenderGroupRule {
    override fun check(item: TenderGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender group. */
data class TenderGroupValidator(var rules: MutableList<TenderGroupRule>) {
    fun validate(item: TenderGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderGroupValidator(): TenderGroupValidator {
    return TenderGroupValidator(
        mutableListOf<TenderGroupRule>(
            TenderGroupCeilingRangeRule(2.2, 4975.0),
            TenderGroupChannelLengthRule(64),
            TenderGroupRequiredChannelRule("tender group needs an entry"),
            TenderGroupCodeLengthRule(64),
        ),
    )
}
