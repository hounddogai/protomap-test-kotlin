package com.acme.transport.freightpay.accessorial.profile

/** A rule a weighted accessorial profile must pass. Check returns its problems, or none. */
interface WeightedAccessorialProfileRule {
    fun check(item: WeightedAccessorialProfile): MutableList<String>
}

data class WeightedAccessorialProfileVehicleIdLengthRule(var maxLength: Int) : WeightedAccessorialProfileRule {
    override fun check(item: WeightedAccessorialProfile): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAccessorialProfileRequiredCodeRule(var message: String) : WeightedAccessorialProfileRule {
    override fun check(item: WeightedAccessorialProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedAccessorialProfileCodeLengthRule(var maxLength: Int) : WeightedAccessorialProfileRule {
    override fun check(item: WeightedAccessorialProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted accessorial profile. */
data class WeightedAccessorialProfileValidator(var rules: MutableList<WeightedAccessorialProfileRule>) {
    fun validate(item: WeightedAccessorialProfile): MutableList<String> {
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

    fun requireValid(item: WeightedAccessorialProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAccessorialProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAccessorialProfileValidator(): WeightedAccessorialProfileValidator {
    return WeightedAccessorialProfileValidator(
        mutableListOf<WeightedAccessorialProfileRule>(
            WeightedAccessorialProfileVehicleIdLengthRule(64),
            WeightedAccessorialProfileRequiredCodeRule("weighted accessorial profile needs an entry"),
            WeightedAccessorialProfileCodeLengthRule(64),
        ),
    )
}
