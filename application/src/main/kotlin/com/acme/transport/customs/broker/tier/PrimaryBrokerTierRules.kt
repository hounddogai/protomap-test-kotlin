package com.acme.transport.customs.broker.tier

/** A rule a primary broker tier must pass. Check returns its problems, or none. */
interface PrimaryBrokerTierRule {
    fun check(item: PrimaryBrokerTier): MutableList<String>
}

data class PrimaryBrokerTierRequiredLabelRule(var message: String) : PrimaryBrokerTierRule {
    override fun check(item: PrimaryBrokerTier): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBrokerTierRequiredChecksumRule(var message: String) : PrimaryBrokerTierRule {
    override fun check(item: PrimaryBrokerTier): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBrokerTierPriorityRangeRule(var min: Int, var max: Int) : PrimaryBrokerTierRule {
    override fun check(item: PrimaryBrokerTier): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBrokerTierRequiredCodeRule(var message: String) : PrimaryBrokerTierRule {
    override fun check(item: PrimaryBrokerTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary broker tier. */
data class PrimaryBrokerTierValidator(var rules: MutableList<PrimaryBrokerTierRule>) {
    fun validate(item: PrimaryBrokerTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryBrokerTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryBrokerTier>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryBrokerTierValidator(): PrimaryBrokerTierValidator {
    return PrimaryBrokerTierValidator(
        mutableListOf<PrimaryBrokerTierRule>(
            PrimaryBrokerTierRequiredLabelRule("primary broker tier needs an entry"),
            PrimaryBrokerTierRequiredChecksumRule("primary broker tier needs this field"),
            PrimaryBrokerTierPriorityRangeRule(120, 255),
            PrimaryBrokerTierRequiredCodeRule("primary broker tier needs this field"),
        ),
    )
}
