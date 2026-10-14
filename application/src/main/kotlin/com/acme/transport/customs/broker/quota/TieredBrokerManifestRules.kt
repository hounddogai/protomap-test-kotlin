package com.acme.transport.customs.broker.quota

/** A rule a tiered broker manifest must pass. Check returns its problems, or none. */
interface TieredBrokerManifestRule {
    fun check(item: TieredBrokerManifest): MutableList<String>
}

data class TieredBrokerManifestRequiredOwnerIdRule(var message: String) : TieredBrokerManifestRule {
    override fun check(item: TieredBrokerManifest): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredBrokerManifestOwnerIdLengthRule(var maxLength: Int) : TieredBrokerManifestRule {
    override fun check(item: TieredBrokerManifest): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredBrokerManifestRequiredTitleRule(var message: String) : TieredBrokerManifestRule {
    override fun check(item: TieredBrokerManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredBrokerManifestCostRangeRule(var min: Double, var max: Double) : TieredBrokerManifestRule {
    override fun check(item: TieredBrokerManifest): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class TieredBrokerManifestCodePrefixRule(var prefix: String) : TieredBrokerManifestRule {
    override fun check(item: TieredBrokerManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredBrokerManifestRequiredCodeRule(var message: String) : TieredBrokerManifestRule {
    override fun check(item: TieredBrokerManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered broker manifest. */
data class TieredBrokerManifestValidator(var rules: MutableList<TieredBrokerManifestRule>) {
    fun validate(item: TieredBrokerManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredBrokerManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredBrokerManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredBrokerManifestValidator(): TieredBrokerManifestValidator {
    return TieredBrokerManifestValidator(
        mutableListOf<TieredBrokerManifestRule>(
            TieredBrokerManifestRequiredOwnerIdRule("tiered broker manifest needs this field"),
            TieredBrokerManifestOwnerIdLengthRule(16),
            TieredBrokerManifestRequiredTitleRule("tiered broker manifest needs a value"),
            TieredBrokerManifestCostRangeRule(4.4, 2207.6),
            TieredBrokerManifestCodePrefixRule("ACM"),
            TieredBrokerManifestRequiredCodeRule("tiered broker manifest needs a value"),
        ),
    )
}
