package com.acme.transport.customs.broker.schedule

/** A rule a capped broker manifest must pass. Check returns its problems, or none. */
interface CappedBrokerManifestRule {
    fun check(item: CappedBrokerManifest): MutableList<String>
}

data class CappedBrokerManifestCostRangeRule(var min: Double, var max: Double) : CappedBrokerManifestRule {
    override fun check(item: CappedBrokerManifest): MutableList<String> {
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

data class CappedBrokerManifestRequiredDriverIdRule(var message: String) : CappedBrokerManifestRule {
    override fun check(item: CappedBrokerManifest): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedBrokerManifestRequiredNameRule(var message: String) : CappedBrokerManifestRule {
    override fun check(item: CappedBrokerManifest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped broker manifest. */
data class CappedBrokerManifestValidator(var rules: MutableList<CappedBrokerManifestRule>) {
    fun validate(item: CappedBrokerManifest): MutableList<String> {
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

    fun requireValid(item: CappedBrokerManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedBrokerManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedBrokerManifestValidator(): CappedBrokerManifestValidator {
    return CappedBrokerManifestValidator(
        mutableListOf<CappedBrokerManifestRule>(
            CappedBrokerManifestCostRangeRule(5.1, 3603.6),
            CappedBrokerManifestRequiredDriverIdRule("capped broker manifest needs this field"),
            CappedBrokerManifestRequiredNameRule("capped broker manifest needs a value"),
        ),
    )
}
