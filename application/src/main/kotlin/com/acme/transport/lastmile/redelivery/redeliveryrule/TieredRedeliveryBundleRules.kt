package com.acme.transport.lastmile.redelivery.redeliveryrule

/** A rule a tiered redelivery bundle must pass. Check returns its problems, or none. */
interface TieredRedeliveryBundleRule {
    fun check(item: TieredRedeliveryBundle): MutableList<String>
}

data class TieredRedeliveryBundleRequiredOwnerIdRule(var message: String) : TieredRedeliveryBundleRule {
    override fun check(item: TieredRedeliveryBundle): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRedeliveryBundleOwnerIdLengthRule(var maxLength: Int) : TieredRedeliveryBundleRule {
    override fun check(item: TieredRedeliveryBundle): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredRedeliveryBundleRevisionRangeRule(var min: Int, var max: Int) : TieredRedeliveryBundleRule {
    override fun check(item: TieredRedeliveryBundle): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredRedeliveryBundleLimitRangeRule(var min: Int, var max: Int) : TieredRedeliveryBundleRule {
    override fun check(item: TieredRedeliveryBundle): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredRedeliveryBundleCodeLengthRule(var maxLength: Int) : TieredRedeliveryBundleRule {
    override fun check(item: TieredRedeliveryBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered redelivery bundle. */
data class TieredRedeliveryBundleValidator(var rules: MutableList<TieredRedeliveryBundleRule>) {
    fun validate(item: TieredRedeliveryBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredRedeliveryBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredRedeliveryBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredRedeliveryBundleValidator(): TieredRedeliveryBundleValidator {
    return TieredRedeliveryBundleValidator(
        mutableListOf<TieredRedeliveryBundleRule>(
            TieredRedeliveryBundleRequiredOwnerIdRule("tiered redelivery bundle needs a value"),
            TieredRedeliveryBundleOwnerIdLengthRule(1000),
            TieredRedeliveryBundleRevisionRangeRule(80, 3600),
            TieredRedeliveryBundleLimitRangeRule(255, 1000),
            TieredRedeliveryBundleCodeLengthRule(32),
        ),
    )
}
