package com.acme.transport.network.node.forecast

import com.acme.transport.kit.text.normalize

/** A rule a node forecast must pass. Check returns its problems, or none. */
interface NodeForecastRule {
    fun check(item: NodeForecast): MutableList<String>
}

data class NodeForecastCreatedAtRangeRule(var min: Int, var max: Int) : NodeForecastRule {
    override fun check(item: NodeForecast): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NodeForecastCodeLengthRule(var maxLength: Int) : NodeForecastRule {
    override fun check(item: NodeForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeForecastCountryLengthRule(var maxLength: Int) : NodeForecastRule {
    override fun check(item: NodeForecast): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeForecastRequiredCountryRule(var message: String) : NodeForecastRule {
    override fun check(item: NodeForecast): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeForecastLaneLengthRule(var maxLength: Int) : NodeForecastRule {
    override fun check(item: NodeForecast): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeForecastAllowedCountryRule(var allowed: MutableList<String>) : NodeForecastRule {
    override fun check(item: NodeForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node forecast. */
data class NodeForecastValidator(var rules: MutableList<NodeForecastRule>) {
    fun validate(item: NodeForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeForecastValidator(): NodeForecastValidator {
    return NodeForecastValidator(
        mutableListOf<NodeForecastRule>(
            NodeForecastCreatedAtRangeRule(1000, 86400),
            NodeForecastCodeLengthRule(80),
            NodeForecastCountryLengthRule(80),
            NodeForecastRequiredCountryRule("node forecast needs this field"),
            NodeForecastLaneLengthRule(32),
            NodeForecastAllowedCountryRule(mutableListOf<String>("lenient", "primary", "global")),
        ),
    )
}
