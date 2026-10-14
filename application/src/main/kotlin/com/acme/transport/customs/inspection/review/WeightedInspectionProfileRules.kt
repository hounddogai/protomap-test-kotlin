package com.acme.transport.customs.inspection.review

/** A rule a weighted inspection profile must pass. Check returns its problems, or none. */
interface WeightedInspectionProfileRule {
    fun check(item: WeightedInspectionProfile): MutableList<String>
}

data class WeightedInspectionProfileRequiredDepotRule(var message: String) : WeightedInspectionProfileRule {
    override fun check(item: WeightedInspectionProfile): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionProfileDepotLengthRule(var maxLength: Int) : WeightedInspectionProfileRule {
    override fun check(item: WeightedInspectionProfile): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionProfileCodePrefixRule(var prefix: String) : WeightedInspectionProfileRule {
    override fun check(item: WeightedInspectionProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionProfileRequiredNameRule(var message: String) : WeightedInspectionProfileRule {
    override fun check(item: WeightedInspectionProfile): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted inspection profile. */
data class WeightedInspectionProfileValidator(var rules: MutableList<WeightedInspectionProfileRule>) {
    fun validate(item: WeightedInspectionProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedInspectionProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedInspectionProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedInspectionProfileValidator(): WeightedInspectionProfileValidator {
    return WeightedInspectionProfileValidator(
        mutableListOf<WeightedInspectionProfileRule>(
            WeightedInspectionProfileRequiredDepotRule("weighted inspection profile needs a value"),
            WeightedInspectionProfileDepotLengthRule(32),
            WeightedInspectionProfileCodePrefixRule("Q"),
            WeightedInspectionProfileRequiredNameRule("weighted inspection profile needs an entry"),
        ),
    )
}
