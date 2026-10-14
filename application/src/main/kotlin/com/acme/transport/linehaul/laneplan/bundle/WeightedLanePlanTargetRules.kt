package com.acme.transport.linehaul.laneplan.bundle

/** A rule a weighted lane plan target must pass. Check returns its problems, or none. */
interface WeightedLanePlanTargetRule {
    fun check(item: WeightedLanePlanTarget): MutableList<String>
}

data class WeightedLanePlanTargetRequiredDescriptionRule(var message: String) : WeightedLanePlanTargetRule {
    override fun check(item: WeightedLanePlanTarget): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedLanePlanTargetRequiredCurrencyRule(var message: String) : WeightedLanePlanTargetRule {
    override fun check(item: WeightedLanePlanTarget): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedLanePlanTargetCodePrefixRule(var prefix: String) : WeightedLanePlanTargetRule {
    override fun check(item: WeightedLanePlanTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedLanePlanTargetRequiredNameRule(var message: String) : WeightedLanePlanTargetRule {
    override fun check(item: WeightedLanePlanTarget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedLanePlanTargetNameLengthRule(var maxLength: Int) : WeightedLanePlanTargetRule {
    override fun check(item: WeightedLanePlanTarget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted lane plan target. */
data class WeightedLanePlanTargetValidator(var rules: MutableList<WeightedLanePlanTargetRule>) {
    fun validate(item: WeightedLanePlanTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedLanePlanTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedLanePlanTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedLanePlanTargetValidator(): WeightedLanePlanTargetValidator {
    return WeightedLanePlanTargetValidator(
        mutableListOf<WeightedLanePlanTargetRule>(
            WeightedLanePlanTargetRequiredDescriptionRule("weighted lane plan target needs an entry"),
            WeightedLanePlanTargetRequiredCurrencyRule("weighted lane plan target needs a value"),
            WeightedLanePlanTargetCodePrefixRule("ACM"),
            WeightedLanePlanTargetRequiredNameRule("weighted lane plan target needs this field"),
            WeightedLanePlanTargetNameLengthRule(255),
        ),
    )
}
