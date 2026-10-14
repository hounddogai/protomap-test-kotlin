package com.acme.transport.emissions.fuelburn.request

import com.acme.transport.kit.text.normalize

/** A rule a fuel burn request must pass. Check returns its problems, or none. */
interface FuelBurnRequestRule {
    fun check(item: FuelBurnRequest): MutableList<String>
}

data class FuelBurnRequestBatchSizeRangeRule(var min: Int, var max: Int) : FuelBurnRequestRule {
    override fun check(item: FuelBurnRequest): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class FuelBurnRequestRegionLengthRule(var maxLength: Int) : FuelBurnRequestRule {
    override fun check(item: FuelBurnRequest): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnRequestAllowedCodeRule(var allowed: MutableList<String>) : FuelBurnRequestRule {
    override fun check(item: FuelBurnRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnRequestNameLengthRule(var maxLength: Int) : FuelBurnRequestRule {
    override fun check(item: FuelBurnRequest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnRequestRequiredCodeRule(var message: String) : FuelBurnRequestRule {
    override fun check(item: FuelBurnRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn request. */
data class FuelBurnRequestValidator(var rules: MutableList<FuelBurnRequestRule>) {
    fun validate(item: FuelBurnRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnRequestValidator(): FuelBurnRequestValidator {
    return FuelBurnRequestValidator(
        mutableListOf<FuelBurnRequestRule>(
            FuelBurnRequestBatchSizeRangeRule(32, 80),
            FuelBurnRequestRegionLengthRule(86400),
            FuelBurnRequestAllowedCodeRule(mutableListOf<String>("manual", "scheduled", "seasonal")),
            FuelBurnRequestNameLengthRule(3600),
            FuelBurnRequestRequiredCodeRule("fuel burn request needs an entry"),
        ),
    )
}
