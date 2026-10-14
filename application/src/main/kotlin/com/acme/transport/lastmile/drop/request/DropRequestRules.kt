package com.acme.transport.lastmile.drop.request

/** A rule a drop request must pass. Check returns its problems, or none. */
interface DropRequestRule {
    fun check(item: DropRequest): MutableList<String>
}

data class DropRequestRequiredCodeRule(var message: String) : DropRequestRule {
    override fun check(item: DropRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropRequestRequiredCarrierRule(var message: String) : DropRequestRule {
    override fun check(item: DropRequest): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropRequestMinimumRangeRule(var min: Double, var max: Double) : DropRequestRule {
    override fun check(item: DropRequest): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop request. */
data class DropRequestValidator(var rules: MutableList<DropRequestRule>) {
    fun validate(item: DropRequest): MutableList<String> {
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

    fun requireValid(item: DropRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultDropRequestValidator(): DropRequestValidator {
    return DropRequestValidator(
        mutableListOf<DropRequestRule>(
            DropRequestRequiredCodeRule("drop request needs a value"),
            DropRequestRequiredCarrierRule("drop request needs this field"),
            DropRequestMinimumRangeRule(2.8, 1250.5),
        ),
    )
}
