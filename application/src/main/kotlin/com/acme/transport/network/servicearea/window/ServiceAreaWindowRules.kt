package com.acme.transport.network.servicearea.window

/** A rule a service area window must pass. Check returns its problems, or none. */
interface ServiceAreaWindowRule {
    fun check(item: ServiceAreaWindow): MutableList<String>
}

data class ServiceAreaWindowTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : ServiceAreaWindowRule {
    override fun check(item: ServiceAreaWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class ServiceAreaWindowRequiredVehicleIdRule(var message: String) : ServiceAreaWindowRule {
    override fun check(item: ServiceAreaWindow): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaWindowPalletsRangeRule(var min: Int, var max: Int) : ServiceAreaWindowRule {
    override fun check(item: ServiceAreaWindow): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaWindowVehicleIdLengthRule(var maxLength: Int) : ServiceAreaWindowRule {
    override fun check(item: ServiceAreaWindow): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area window. */
data class ServiceAreaWindowValidator(var rules: MutableList<ServiceAreaWindowRule>) {
    fun validate(item: ServiceAreaWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServiceAreaWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaWindowValidator(): ServiceAreaWindowValidator {
    return ServiceAreaWindowValidator(
        mutableListOf<ServiceAreaWindowRule>(
            ServiceAreaWindowTagsLimitRule(255, mutableListOf<String>("rolling", "capped")),
            ServiceAreaWindowRequiredVehicleIdRule("service area window needs this field"),
            ServiceAreaWindowPalletsRangeRule(255, 86400),
            ServiceAreaWindowVehicleIdLengthRule(3600),
        ),
    )
}
