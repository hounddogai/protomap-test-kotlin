package com.acme.transport.lastmile.parcel.metric

/** A rule a parcel exception must pass. Check returns its problems, or none. */
interface ParcelExceptionRule {
    fun check(item: ParcelException): MutableList<String>
}

data class ParcelExceptionCodePrefixRule(var prefix: String) : ParcelExceptionRule {
    override fun check(item: ParcelException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ParcelExceptionShiftCodeLengthRule(var maxLength: Int) : ParcelExceptionRule {
    override fun check(item: ParcelException): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelExceptionDockLengthRule(var maxLength: Int) : ParcelExceptionRule {
    override fun check(item: ParcelException): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelExceptionRequiredShiftCodeRule(var message: String) : ParcelExceptionRule {
    override fun check(item: ParcelException): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelExceptionLoadKgRangeRule(var min: Double, var max: Double) : ParcelExceptionRule {
    override fun check(item: ParcelException): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class ParcelExceptionRequiredDockRule(var message: String) : ParcelExceptionRule {
    override fun check(item: ParcelException): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel exception. */
data class ParcelExceptionValidator(var rules: MutableList<ParcelExceptionRule>) {
    fun validate(item: ParcelException): MutableList<String> {
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

    fun requireValid(item: ParcelException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelException>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelExceptionValidator(): ParcelExceptionValidator {
    return ParcelExceptionValidator(
        mutableListOf<ParcelExceptionRule>(
            ParcelExceptionCodePrefixRule("Q"),
            ParcelExceptionShiftCodeLengthRule(255),
            ParcelExceptionDockLengthRule(120),
            ParcelExceptionRequiredShiftCodeRule("parcel exception needs this field"),
            ParcelExceptionLoadKgRangeRule(5.8, 724.8),
            ParcelExceptionRequiredDockRule("parcel exception needs this field"),
        ),
    )
}
