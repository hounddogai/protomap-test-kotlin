package com.acme.transport.network.hub.batch

/** A rule a hub rollup must pass. Check returns its problems, or none. */
interface HubRollupRule {
    fun check(item: HubRollup): MutableList<String>
}

data class HubRollupRequiredRouteCodeRule(var message: String) : HubRollupRule {
    override fun check(item: HubRollup): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubRollupLabelLengthRule(var maxLength: Int) : HubRollupRule {
    override fun check(item: HubRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubRollupRequiredLabelRule(var message: String) : HubRollupRule {
    override fun check(item: HubRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub rollup. */
data class HubRollupValidator(var rules: MutableList<HubRollupRule>) {
    fun validate(item: HubRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultHubRollupValidator(): HubRollupValidator {
    return HubRollupValidator(
        mutableListOf<HubRollupRule>(
            HubRollupRequiredRouteCodeRule("hub rollup needs an entry"),
            HubRollupLabelLengthRule(86400),
            HubRollupRequiredLabelRule("hub rollup needs a value"),
        ),
    )
}
