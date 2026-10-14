package com.acme.transport.freightpay.accessorial.profile

/** A rule a primary accessorial group must pass. Check returns its problems, or none. */
interface PrimaryAccessorialGroupRule {
    fun check(item: PrimaryAccessorialGroup): MutableList<String>
}

data class PrimaryAccessorialGroupCodeLengthRule(var maxLength: Int) : PrimaryAccessorialGroupRule {
    override fun check(item: PrimaryAccessorialGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAccessorialGroupBatchSizeRangeRule(var min: Int, var max: Int) : PrimaryAccessorialGroupRule {
    override fun check(item: PrimaryAccessorialGroup): MutableList<String> {
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

data class PrimaryAccessorialGroupRequiredCodeRule(var message: String) : PrimaryAccessorialGroupRule {
    override fun check(item: PrimaryAccessorialGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAccessorialGroupRequiredLocaleRule(var message: String) : PrimaryAccessorialGroupRule {
    override fun check(item: PrimaryAccessorialGroup): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAccessorialGroupCodePrefixRule(var prefix: String) : PrimaryAccessorialGroupRule {
    override fun check(item: PrimaryAccessorialGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAccessorialGroupTitleLengthRule(var maxLength: Int) : PrimaryAccessorialGroupRule {
    override fun check(item: PrimaryAccessorialGroup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary accessorial group. */
data class PrimaryAccessorialGroupValidator(var rules: MutableList<PrimaryAccessorialGroupRule>) {
    fun validate(item: PrimaryAccessorialGroup): MutableList<String> {
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

    fun requireValid(item: PrimaryAccessorialGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryAccessorialGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryAccessorialGroupValidator(): PrimaryAccessorialGroupValidator {
    return PrimaryAccessorialGroupValidator(
        mutableListOf<PrimaryAccessorialGroupRule>(
            PrimaryAccessorialGroupCodeLengthRule(80),
            PrimaryAccessorialGroupBatchSizeRangeRule(80, 1000),
            PrimaryAccessorialGroupRequiredCodeRule("primary accessorial group needs this field"),
            PrimaryAccessorialGroupRequiredLocaleRule("primary accessorial group needs this field"),
            PrimaryAccessorialGroupCodePrefixRule("OPS"),
            PrimaryAccessorialGroupTitleLengthRule(120),
        ),
    )
}
