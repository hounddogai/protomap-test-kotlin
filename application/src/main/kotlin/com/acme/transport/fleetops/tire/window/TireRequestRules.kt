package com.acme.transport.fleetops.tire.window

/** A rule a tire request must pass. Check returns its problems, or none. */
interface TireRequestRule {
    fun check(item: TireRequest): MutableList<String>
}

data class TireRequestMarginRangeRule(var min: Double, var max: Double) : TireRequestRule {
    override fun check(item: TireRequest): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TireRequestRatioRangeRule(var min: Double, var max: Double) : TireRequestRule {
    override fun check(item: TireRequest): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class TireRequestRequiredOwnerIdRule(var message: String) : TireRequestRule {
    override fun check(item: TireRequest): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireRequestRequiredExternalRefRule(var message: String) : TireRequestRule {
    override fun check(item: TireRequest): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireRequestRequiredCodeRule(var message: String) : TireRequestRule {
    override fun check(item: TireRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireRequestCodeLengthRule(var maxLength: Int) : TireRequestRule {
    override fun check(item: TireRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire request. */
data class TireRequestValidator(var rules: MutableList<TireRequestRule>) {
    fun validate(item: TireRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultTireRequestValidator(): TireRequestValidator {
    return TireRequestValidator(
        mutableListOf<TireRequestRule>(
            TireRequestMarginRangeRule(4.2, 3503.4),
            TireRequestRatioRangeRule(8.2, 985.4),
            TireRequestRequiredOwnerIdRule("tire request needs an entry"),
            TireRequestRequiredExternalRefRule("tire request needs this field"),
            TireRequestRequiredCodeRule("tire request needs this field"),
            TireRequestCodeLengthRule(16),
        ),
    )
}
