package com.acme.transport.emissions.routeoffset.slot

/** A rule a capped route offset request must pass. Check returns its problems, or none. */
interface CappedRouteOffsetRequestRule {
    fun check(item: CappedRouteOffsetRequest): MutableList<String>
}

data class CappedRouteOffsetRequestLaneLengthRule(var maxLength: Int) : CappedRouteOffsetRequestRule {
    override fun check(item: CappedRouteOffsetRequest): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRouteOffsetRequestSequenceRangeRule(
    var min: Int,
    var max: Int,
) : CappedRouteOffsetRequestRule {
    override fun check(item: CappedRouteOffsetRequest): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedRouteOffsetRequestRequiredLaneRule(var message: String) : CappedRouteOffsetRequestRule {
    override fun check(item: CappedRouteOffsetRequest): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped route offset request. */
data class CappedRouteOffsetRequestValidator(var rules: MutableList<CappedRouteOffsetRequestRule>) {
    fun validate(item: CappedRouteOffsetRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedRouteOffsetRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedRouteOffsetRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedRouteOffsetRequestValidator(): CappedRouteOffsetRequestValidator {
    return CappedRouteOffsetRequestValidator(
        mutableListOf<CappedRouteOffsetRequestRule>(
            CappedRouteOffsetRequestLaneLengthRule(32),
            CappedRouteOffsetRequestSequenceRangeRule(1000, 3600),
            CappedRouteOffsetRequestRequiredLaneRule("capped route offset request needs a value"),
        ),
    )
}
