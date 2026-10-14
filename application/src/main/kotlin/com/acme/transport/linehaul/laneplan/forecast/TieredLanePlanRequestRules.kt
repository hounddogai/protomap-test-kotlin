package com.acme.transport.linehaul.laneplan.forecast

/** A rule a tiered lane plan request must pass. Check returns its problems, or none. */
interface TieredLanePlanRequestRule {
    fun check(item: TieredLanePlanRequest): MutableList<String>
}

data class TieredLanePlanRequestDepotLengthRule(var maxLength: Int) : TieredLanePlanRequestRule {
    override fun check(item: TieredLanePlanRequest): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredLanePlanRequestCodeLengthRule(var maxLength: Int) : TieredLanePlanRequestRule {
    override fun check(item: TieredLanePlanRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredLanePlanRequestRequiredDepotRule(var message: String) : TieredLanePlanRequestRule {
    override fun check(item: TieredLanePlanRequest): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredLanePlanRequestBatchSizeRangeRule(var min: Int, var max: Int) : TieredLanePlanRequestRule {
    override fun check(item: TieredLanePlanRequest): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredLanePlanRequestRequiredCodeRule(var message: String) : TieredLanePlanRequestRule {
    override fun check(item: TieredLanePlanRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredLanePlanRequestRequiredLabelRule(var message: String) : TieredLanePlanRequestRule {
    override fun check(item: TieredLanePlanRequest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered lane plan request. */
data class TieredLanePlanRequestValidator(var rules: MutableList<TieredLanePlanRequestRule>) {
    fun validate(item: TieredLanePlanRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredLanePlanRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredLanePlanRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredLanePlanRequestValidator(): TieredLanePlanRequestValidator {
    return TieredLanePlanRequestValidator(
        mutableListOf<TieredLanePlanRequestRule>(
            TieredLanePlanRequestDepotLengthRule(80),
            TieredLanePlanRequestCodeLengthRule(86400),
            TieredLanePlanRequestRequiredDepotRule("tiered lane plan request needs this field"),
            TieredLanePlanRequestBatchSizeRangeRule(32, 80),
            TieredLanePlanRequestRequiredCodeRule("tiered lane plan request needs a value"),
            TieredLanePlanRequestRequiredLabelRule("tiered lane plan request needs this field"),
        ),
    )
}
