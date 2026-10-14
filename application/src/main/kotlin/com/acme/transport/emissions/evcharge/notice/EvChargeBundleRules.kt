package com.acme.transport.emissions.evcharge.notice

/** A rule a ev charge bundle must pass. Check returns its problems, or none. */
interface EvChargeBundleRule {
    fun check(item: EvChargeBundle): MutableList<String>
}

data class EvChargeBundleRequiredCodeRule(var message: String) : EvChargeBundleRule {
    override fun check(item: EvChargeBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeBundleRequiredNameRule(var message: String) : EvChargeBundleRule {
    override fun check(item: EvChargeBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeBundleDiscountRangeRule(var min: Double, var max: Double) : EvChargeBundleRule {
    override fun check(item: EvChargeBundle): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class EvChargeBundleLoadKgRangeRule(var min: Double, var max: Double) : EvChargeBundleRule {
    override fun check(item: EvChargeBundle): MutableList<String> {
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

data class EvChargeBundleCodeLengthRule(var maxLength: Int) : EvChargeBundleRule {
    override fun check(item: EvChargeBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge bundle. */
data class EvChargeBundleValidator(var rules: MutableList<EvChargeBundleRule>) {
    fun validate(item: EvChargeBundle): MutableList<String> {
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

    fun requireValid(item: EvChargeBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeBundleValidator(): EvChargeBundleValidator {
    return EvChargeBundleValidator(
        mutableListOf<EvChargeBundleRule>(
            EvChargeBundleRequiredCodeRule("ev charge bundle needs this field"),
            EvChargeBundleRequiredNameRule("ev charge bundle needs a value"),
            EvChargeBundleDiscountRangeRule(5.7, 1234.4),
            EvChargeBundleLoadKgRangeRule(1.6, 2493.3),
            EvChargeBundleCodeLengthRule(3600),
        ),
    )
}
