package com.acme.transport.network.transittime.manifest

/** A rule a default transit time manifest must pass. Check returns its problems, or none. */
interface DefaultTransitTimeManifestRule {
    fun check(item: DefaultTransitTimeManifest): MutableList<String>
}

data class DefaultTransitTimeManifestCodeLengthRule(var maxLength: Int) : DefaultTransitTimeManifestRule {
    override fun check(item: DefaultTransitTimeManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeManifestAmountRangeRule(
    var min: Double,
    var max: Double,
) : DefaultTransitTimeManifestRule {
    override fun check(item: DefaultTransitTimeManifest): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class DefaultTransitTimeManifestVehicleIdLengthRule(var maxLength: Int) : DefaultTransitTimeManifestRule {
    override fun check(item: DefaultTransitTimeManifest): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeManifestPinnedGuardRule(var limit: Int) : DefaultTransitTimeManifestRule {
    override fun check(item: DefaultTransitTimeManifest): MutableList<String> {
        if (item.pinned && item.stopCount > limit) {
            return mutableListOf<String>("stop count ${item.stopCount} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeManifestRequiredTitleRule(var message: String) : DefaultTransitTimeManifestRule {
    override fun check(item: DefaultTransitTimeManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeManifestRequiredVehicleIdRule(
    var message: String,
) : DefaultTransitTimeManifestRule {
    override fun check(item: DefaultTransitTimeManifest): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default transit time manifest. */
data class DefaultTransitTimeManifestValidator(var rules: MutableList<DefaultTransitTimeManifestRule>) {
    fun validate(item: DefaultTransitTimeManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultTransitTimeManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTransitTimeManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTransitTimeManifestValidator(): DefaultTransitTimeManifestValidator {
    return DefaultTransitTimeManifestValidator(
        mutableListOf<DefaultTransitTimeManifestRule>(
            DefaultTransitTimeManifestCodeLengthRule(64),
            DefaultTransitTimeManifestAmountRangeRule(2.4, 1910.7),
            DefaultTransitTimeManifestVehicleIdLengthRule(3600),
            DefaultTransitTimeManifestPinnedGuardRule(86400),
            DefaultTransitTimeManifestRequiredTitleRule("default transit time manifest needs a value"),
            DefaultTransitTimeManifestRequiredVehicleIdRule("default transit time manifest needs a value"),
        ),
    )
}
