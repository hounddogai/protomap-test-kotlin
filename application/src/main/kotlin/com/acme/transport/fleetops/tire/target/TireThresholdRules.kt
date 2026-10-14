package com.acme.transport.fleetops.tire.target

/** A rule a tire threshold must pass. Check returns its problems, or none. */
interface TireThresholdRule {
    fun check(item: TireThreshold): MutableList<String>
}

data class TireThresholdCodePrefixRule(var prefix: String) : TireThresholdRule {
    override fun check(item: TireThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TireThresholdVehicleIdLengthRule(var maxLength: Int) : TireThresholdRule {
    override fun check(item: TireThreshold): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireThresholdRequiredTitleRule(var message: String) : TireThresholdRule {
    override fun check(item: TireThreshold): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireThresholdCarrierLengthRule(var maxLength: Int) : TireThresholdRule {
    override fun check(item: TireThreshold): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireThresholdRequiredVehicleIdRule(var message: String) : TireThresholdRule {
    override fun check(item: TireThreshold): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire threshold. */
data class TireThresholdValidator(var rules: MutableList<TireThresholdRule>) {
    fun validate(item: TireThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultTireThresholdValidator(): TireThresholdValidator {
    return TireThresholdValidator(
        mutableListOf<TireThresholdRule>(
            TireThresholdCodePrefixRule("OPS"),
            TireThresholdVehicleIdLengthRule(1000),
            TireThresholdRequiredTitleRule("tire threshold needs an entry"),
            TireThresholdCarrierLengthRule(64),
            TireThresholdRequiredVehicleIdRule("tire threshold needs a value"),
        ),
    )
}
