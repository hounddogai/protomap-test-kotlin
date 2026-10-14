package com.acme.transport.lastmile.parcel.claim

/** A rule a pending parcel override must pass. Check returns its problems, or none. */
interface PendingParcelOverrideRule {
    fun check(item: PendingParcelOverride): MutableList<String>
}

data class PendingParcelOverrideCodePrefixRule(var prefix: String) : PendingParcelOverrideRule {
    override fun check(item: PendingParcelOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingParcelOverrideFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : PendingParcelOverrideRule {
    override fun check(item: PendingParcelOverride): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingParcelOverrideDriverIdLengthRule(var maxLength: Int) : PendingParcelOverrideRule {
    override fun check(item: PendingParcelOverride): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingParcelOverrideCodeLengthRule(var maxLength: Int) : PendingParcelOverrideRule {
    override fun check(item: PendingParcelOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingParcelOverrideNameLengthRule(var maxLength: Int) : PendingParcelOverrideRule {
    override fun check(item: PendingParcelOverride): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending parcel override. */
data class PendingParcelOverrideValidator(var rules: MutableList<PendingParcelOverrideRule>) {
    fun validate(item: PendingParcelOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingParcelOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingParcelOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingParcelOverrideValidator(): PendingParcelOverrideValidator {
    return PendingParcelOverrideValidator(
        mutableListOf<PendingParcelOverrideRule>(
            PendingParcelOverrideCodePrefixRule("REF"),
            PendingParcelOverrideFloorValueRangeRule(5.1, 3183.6),
            PendingParcelOverrideDriverIdLengthRule(3600),
            PendingParcelOverrideCodeLengthRule(120),
            PendingParcelOverrideNameLengthRule(32),
        ),
    )
}
