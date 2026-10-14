package com.acme.transport.lastmile.parcel.milestone

/** A rule a primary parcel entry must pass. Check returns its problems, or none. */
interface PrimaryParcelEntryRule {
    fun check(item: PrimaryParcelEntry): MutableList<String>
}

data class PrimaryParcelEntryNameLengthRule(var maxLength: Int) : PrimaryParcelEntryRule {
    override fun check(item: PrimaryParcelEntry): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryParcelEntryCodeLengthRule(var maxLength: Int) : PrimaryParcelEntryRule {
    override fun check(item: PrimaryParcelEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryParcelEntryVehicleIdLengthRule(var maxLength: Int) : PrimaryParcelEntryRule {
    override fun check(item: PrimaryParcelEntry): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryParcelEntryCodePrefixRule(var prefix: String) : PrimaryParcelEntryRule {
    override fun check(item: PrimaryParcelEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryParcelEntryStopCountRangeRule(var min: Int, var max: Int) : PrimaryParcelEntryRule {
    override fun check(item: PrimaryParcelEntry): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryParcelEntryMaximumRangeRule(var min: Double, var max: Double) : PrimaryParcelEntryRule {
    override fun check(item: PrimaryParcelEntry): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary parcel entry. */
data class PrimaryParcelEntryValidator(var rules: MutableList<PrimaryParcelEntryRule>) {
    fun validate(item: PrimaryParcelEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryParcelEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryParcelEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryParcelEntryValidator(): PrimaryParcelEntryValidator {
    return PrimaryParcelEntryValidator(
        mutableListOf<PrimaryParcelEntryRule>(
            PrimaryParcelEntryNameLengthRule(120),
            PrimaryParcelEntryCodeLengthRule(16),
            PrimaryParcelEntryVehicleIdLengthRule(255),
            PrimaryParcelEntryCodePrefixRule("OPS"),
            PrimaryParcelEntryStopCountRangeRule(16, 255),
            PrimaryParcelEntryMaximumRangeRule(2.5, 4452.0),
        ),
    )
}
