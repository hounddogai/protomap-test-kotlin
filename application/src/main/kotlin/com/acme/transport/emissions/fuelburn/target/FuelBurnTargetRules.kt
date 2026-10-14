package com.acme.transport.emissions.fuelburn.target

/** A rule a fuel burn target must pass. Check returns its problems, or none. */
interface FuelBurnTargetRule {
    fun check(item: FuelBurnTarget): MutableList<String>
}

data class FuelBurnTargetNameLengthRule(var maxLength: Int) : FuelBurnTargetRule {
    override fun check(item: FuelBurnTarget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnTargetChannelLengthRule(var maxLength: Int) : FuelBurnTargetRule {
    override fun check(item: FuelBurnTarget): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnTargetCodePrefixRule(var prefix: String) : FuelBurnTargetRule {
    override fun check(item: FuelBurnTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnTargetRequiredCodeRule(var message: String) : FuelBurnTargetRule {
    override fun check(item: FuelBurnTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnTargetChecksumLengthRule(var maxLength: Int) : FuelBurnTargetRule {
    override fun check(item: FuelBurnTarget): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn target. */
data class FuelBurnTargetValidator(var rules: MutableList<FuelBurnTargetRule>) {
    fun validate(item: FuelBurnTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnTargetValidator(): FuelBurnTargetValidator {
    return FuelBurnTargetValidator(
        mutableListOf<FuelBurnTargetRule>(
            FuelBurnTargetNameLengthRule(80),
            FuelBurnTargetChannelLengthRule(3600),
            FuelBurnTargetCodePrefixRule("ACM"),
            FuelBurnTargetRequiredCodeRule("fuel burn target needs a value"),
            FuelBurnTargetChecksumLengthRule(80),
        ),
    )
}
