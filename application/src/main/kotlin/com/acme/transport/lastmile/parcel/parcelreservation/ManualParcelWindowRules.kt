package com.acme.transport.lastmile.parcel.parcelreservation

/** A rule a manual parcel window must pass. Check returns its problems, or none. */
interface ManualParcelWindowRule {
    fun check(item: ManualParcelWindow): MutableList<String>
}

data class ManualParcelWindowCodeLengthRule(var maxLength: Int) : ManualParcelWindowRule {
    override fun check(item: ManualParcelWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualParcelWindowCreatedAtRangeRule(var min: Int, var max: Int) : ManualParcelWindowRule {
    override fun check(item: ManualParcelWindow): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualParcelWindowWindowDaysRangeRule(var min: Int, var max: Int) : ManualParcelWindowRule {
    override fun check(item: ManualParcelWindow): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class ManualParcelWindowRequiredChannelRule(var message: String) : ManualParcelWindowRule {
    override fun check(item: ManualParcelWindow): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualParcelWindowNameLengthRule(var maxLength: Int) : ManualParcelWindowRule {
    override fun check(item: ManualParcelWindow): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualParcelWindowCodePrefixRule(var prefix: String) : ManualParcelWindowRule {
    override fun check(item: ManualParcelWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual parcel window. */
data class ManualParcelWindowValidator(var rules: MutableList<ManualParcelWindowRule>) {
    fun validate(item: ManualParcelWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualParcelWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualParcelWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultManualParcelWindowValidator(): ManualParcelWindowValidator {
    return ManualParcelWindowValidator(
        mutableListOf<ManualParcelWindowRule>(
            ManualParcelWindowCodeLengthRule(1000),
            ManualParcelWindowCreatedAtRangeRule(120, 120),
            ManualParcelWindowWindowDaysRangeRule(80, 120),
            ManualParcelWindowRequiredChannelRule("manual parcel window needs a value"),
            ManualParcelWindowNameLengthRule(64),
            ManualParcelWindowCodePrefixRule("ACM"),
        ),
    )
}
