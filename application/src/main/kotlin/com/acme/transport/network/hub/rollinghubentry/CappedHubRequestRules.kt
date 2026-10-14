package com.acme.transport.network.hub.rollinghubentry

/** A rule a capped hub request must pass. Check returns its problems, or none. */
interface CappedHubRequestRule {
    fun check(item: CappedHubRequest): MutableList<String>
}

data class CappedHubRequestRequiredLabelRule(var message: String) : CappedHubRequestRule {
    override fun check(item: CappedHubRequest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedHubRequestLabelLengthRule(var maxLength: Int) : CappedHubRequestRule {
    override fun check(item: CappedHubRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedHubRequestCodePrefixRule(var prefix: String) : CappedHubRequestRule {
    override fun check(item: CappedHubRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedHubRequestContactEmailLengthRule(var maxLength: Int) : CappedHubRequestRule {
    override fun check(item: CappedHubRequest): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedHubRequestStartsAtRangeRule(var min: Int, var max: Int) : CappedHubRequestRule {
    override fun check(item: CappedHubRequest): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class CappedHubRequestCostRangeRule(var min: Double, var max: Double) : CappedHubRequestRule {
    override fun check(item: CappedHubRequest): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped hub request. */
data class CappedHubRequestValidator(var rules: MutableList<CappedHubRequestRule>) {
    fun validate(item: CappedHubRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedHubRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedHubRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedHubRequestValidator(): CappedHubRequestValidator {
    return CappedHubRequestValidator(
        mutableListOf<CappedHubRequestRule>(
            CappedHubRequestRequiredLabelRule("capped hub request needs this field"),
            CappedHubRequestLabelLengthRule(1000),
            CappedHubRequestCodePrefixRule("OPS"),
            CappedHubRequestContactEmailLengthRule(120),
            CappedHubRequestStartsAtRangeRule(1000, 3600),
            CappedHubRequestCostRangeRule(0.7, 1486.1),
        ),
    )
}
