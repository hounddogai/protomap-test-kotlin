package com.acme.transport.fleetops.leasereturn.forecast

/** A rule a weighted lease return profile must pass. Check returns its problems, or none. */
interface WeightedLeaseReturnProfileRule {
    fun check(item: WeightedLeaseReturnProfile): MutableList<String>
}

data class WeightedLeaseReturnProfileRequiredLabelRule(var message: String) : WeightedLeaseReturnProfileRule {
    override fun check(item: WeightedLeaseReturnProfile): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedLeaseReturnProfileRequiredTitleRule(var message: String) : WeightedLeaseReturnProfileRule {
    override fun check(item: WeightedLeaseReturnProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedLeaseReturnProfileRequiredCodeRule(var message: String) : WeightedLeaseReturnProfileRule {
    override fun check(item: WeightedLeaseReturnProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedLeaseReturnProfileCodePrefixRule(var prefix: String) : WeightedLeaseReturnProfileRule {
    override fun check(item: WeightedLeaseReturnProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted lease return profile. */
data class WeightedLeaseReturnProfileValidator(var rules: MutableList<WeightedLeaseReturnProfileRule>) {
    fun validate(item: WeightedLeaseReturnProfile): MutableList<String> {
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

    fun requireValid(item: WeightedLeaseReturnProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedLeaseReturnProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedLeaseReturnProfileValidator(): WeightedLeaseReturnProfileValidator {
    return WeightedLeaseReturnProfileValidator(
        mutableListOf<WeightedLeaseReturnProfileRule>(
            WeightedLeaseReturnProfileRequiredLabelRule("weighted lease return profile needs this field"),
            WeightedLeaseReturnProfileRequiredTitleRule("weighted lease return profile needs a value"),
            WeightedLeaseReturnProfileRequiredCodeRule("weighted lease return profile needs an entry"),
            WeightedLeaseReturnProfileCodePrefixRule("Q"),
        ),
    )
}
