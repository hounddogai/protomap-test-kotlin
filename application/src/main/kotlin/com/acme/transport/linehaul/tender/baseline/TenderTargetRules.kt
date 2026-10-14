package com.acme.transport.linehaul.tender.baseline

/** A rule a tender target must pass. Check returns its problems, or none. */
interface TenderTargetRule {
    fun check(item: TenderTarget): MutableList<String>
}

data class TenderTargetCapacityRangeRule(var min: Int, var max: Int) : TenderTargetRule {
    override fun check(item: TenderTarget): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class TenderTargetAttemptsRangeRule(var min: Int, var max: Int) : TenderTargetRule {
    override fun check(item: TenderTarget): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class TenderTargetVersionRangeRule(var min: Int, var max: Int) : TenderTargetRule {
    override fun check(item: TenderTarget): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender target. */
data class TenderTargetValidator(var rules: MutableList<TenderTargetRule>) {
    fun validate(item: TenderTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderTargetValidator(): TenderTargetValidator {
    return TenderTargetValidator(
        mutableListOf<TenderTargetRule>(
            TenderTargetCapacityRangeRule(80, 120),
            TenderTargetAttemptsRangeRule(64, 64),
            TenderTargetVersionRangeRule(80, 86400),
        ),
    )
}
