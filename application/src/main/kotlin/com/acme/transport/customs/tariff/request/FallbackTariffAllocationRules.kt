package com.acme.transport.customs.tariff.request

/** A rule a fallback tariff allocation must pass. Check returns its problems, or none. */
interface FallbackTariffAllocationRule {
    fun check(item: FallbackTariffAllocation): MutableList<String>
}

data class FallbackTariffAllocationWeightRangeRule(
    var min: Double,
    var max: Double,
) : FallbackTariffAllocationRule {
    override fun check(item: FallbackTariffAllocation): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTariffAllocationRequiredCodeRule(var message: String) : FallbackTariffAllocationRule {
    override fun check(item: FallbackTariffAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTariffAllocationChecksumLengthRule(var maxLength: Int) : FallbackTariffAllocationRule {
    override fun check(item: FallbackTariffAllocation): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTariffAllocationRequiredChecksumRule(var message: String) : FallbackTariffAllocationRule {
    override fun check(item: FallbackTariffAllocation): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback tariff allocation. */
data class FallbackTariffAllocationValidator(var rules: MutableList<FallbackTariffAllocationRule>) {
    fun validate(item: FallbackTariffAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackTariffAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackTariffAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackTariffAllocationValidator(): FallbackTariffAllocationValidator {
    return FallbackTariffAllocationValidator(
        mutableListOf<FallbackTariffAllocationRule>(
            FallbackTariffAllocationWeightRangeRule(3.3, 858.7),
            FallbackTariffAllocationRequiredCodeRule("fallback tariff allocation needs this field"),
            FallbackTariffAllocationChecksumLengthRule(3600),
            FallbackTariffAllocationRequiredChecksumRule("fallback tariff allocation needs a value"),
        ),
    )
}
