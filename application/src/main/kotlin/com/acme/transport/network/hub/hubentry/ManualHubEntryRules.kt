package com.acme.transport.network.hub.hubentry

/** A rule a manual hub entry must pass. Check returns its problems, or none. */
interface ManualHubEntryRule {
    fun check(item: ManualHubEntry): MutableList<String>
}

data class ManualHubEntryRequiredLaneRule(var message: String) : ManualHubEntryRule {
    override fun check(item: ManualHubEntry): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualHubEntryCodePrefixRule(var prefix: String) : ManualHubEntryRule {
    override fun check(item: ManualHubEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualHubEntryRequiredCodeRule(var message: String) : ManualHubEntryRule {
    override fun check(item: ManualHubEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualHubEntryRequiredVehicleIdRule(var message: String) : ManualHubEntryRule {
    override fun check(item: ManualHubEntry): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual hub entry. */
data class ManualHubEntryValidator(var rules: MutableList<ManualHubEntryRule>) {
    fun validate(item: ManualHubEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualHubEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualHubEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultManualHubEntryValidator(): ManualHubEntryValidator {
    return ManualHubEntryValidator(
        mutableListOf<ManualHubEntryRule>(
            ManualHubEntryRequiredLaneRule("manual hub entry needs a value"),
            ManualHubEntryCodePrefixRule("ACM"),
            ManualHubEntryRequiredCodeRule("manual hub entry needs this field"),
            ManualHubEntryRequiredVehicleIdRule("manual hub entry needs this field"),
        ),
    )
}
