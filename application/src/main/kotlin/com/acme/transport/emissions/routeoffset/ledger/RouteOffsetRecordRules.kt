package com.acme.transport.emissions.routeoffset.ledger

/** A rule a route offset record must pass. Check returns its problems, or none. */
interface RouteOffsetRecordRule {
    fun check(item: RouteOffsetRecord): MutableList<String>
}

data class RouteOffsetRecordRequiredNameRule(var message: String) : RouteOffsetRecordRule {
    override fun check(item: RouteOffsetRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetRecordNameLengthRule(var maxLength: Int) : RouteOffsetRecordRule {
    override fun check(item: RouteOffsetRecord): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetRecordCodeLengthRule(var maxLength: Int) : RouteOffsetRecordRule {
    override fun check(item: RouteOffsetRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset record. */
data class RouteOffsetRecordValidator(var rules: MutableList<RouteOffsetRecordRule>) {
    fun validate(item: RouteOffsetRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetRecordValidator(): RouteOffsetRecordValidator {
    return RouteOffsetRecordValidator(
        mutableListOf<RouteOffsetRecordRule>(
            RouteOffsetRecordRequiredNameRule("route offset record needs a value"),
            RouteOffsetRecordNameLengthRule(64),
            RouteOffsetRecordCodeLengthRule(86400),
        ),
    )
}
