package com.acme.transport.emissions.fuelburn.notice

/** A rule a fuel burn mapping must pass. Check returns its problems, or none. */
interface FuelBurnMappingRule {
    fun check(item: FuelBurnMapping): MutableList<String>
}

data class FuelBurnMappingRequiredTitleRule(var message: String) : FuelBurnMappingRule {
    override fun check(item: FuelBurnMapping): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnMappingCodeLengthRule(var maxLength: Int) : FuelBurnMappingRule {
    override fun check(item: FuelBurnMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnMappingNameLengthRule(var maxLength: Int) : FuelBurnMappingRule {
    override fun check(item: FuelBurnMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnMappingLabelLengthRule(var maxLength: Int) : FuelBurnMappingRule {
    override fun check(item: FuelBurnMapping): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnMappingTitleLengthRule(var maxLength: Int) : FuelBurnMappingRule {
    override fun check(item: FuelBurnMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnMappingRequiredCodeRule(var message: String) : FuelBurnMappingRule {
    override fun check(item: FuelBurnMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn mapping. */
data class FuelBurnMappingValidator(var rules: MutableList<FuelBurnMappingRule>) {
    fun validate(item: FuelBurnMapping): MutableList<String> {
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

    fun requireValid(item: FuelBurnMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnMappingValidator(): FuelBurnMappingValidator {
    return FuelBurnMappingValidator(
        mutableListOf<FuelBurnMappingRule>(
            FuelBurnMappingRequiredTitleRule("fuel burn mapping needs a value"),
            FuelBurnMappingCodeLengthRule(255),
            FuelBurnMappingNameLengthRule(86400),
            FuelBurnMappingLabelLengthRule(86400),
            FuelBurnMappingTitleLengthRule(3600),
            FuelBurnMappingRequiredCodeRule("fuel burn mapping needs an entry"),
        ),
    )
}
