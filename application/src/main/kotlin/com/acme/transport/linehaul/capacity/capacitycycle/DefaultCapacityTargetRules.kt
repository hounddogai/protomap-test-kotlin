package com.acme.transport.linehaul.capacity.capacitycycle

import com.acme.transport.kit.text.normalize

/** A rule a default capacity target must pass. Check returns its problems, or none. */
interface DefaultCapacityTargetRule {
    fun check(item: DefaultCapacityTarget): MutableList<String>
}

data class DefaultCapacityTargetAllowedSourceRule(
    var allowed: MutableList<String>,
) : DefaultCapacityTargetRule {
    override fun check(item: DefaultCapacityTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCapacityTargetTitleLengthRule(var maxLength: Int) : DefaultCapacityTargetRule {
    override fun check(item: DefaultCapacityTarget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCapacityTargetCodeLengthRule(var maxLength: Int) : DefaultCapacityTargetRule {
    override fun check(item: DefaultCapacityTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCapacityTargetRouteCodeLengthRule(var maxLength: Int) : DefaultCapacityTargetRule {
    override fun check(item: DefaultCapacityTarget): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCapacityTargetRequiredRouteCodeRule(var message: String) : DefaultCapacityTargetRule {
    override fun check(item: DefaultCapacityTarget): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default capacity target. */
data class DefaultCapacityTargetValidator(var rules: MutableList<DefaultCapacityTargetRule>) {
    fun validate(item: DefaultCapacityTarget): MutableList<String> {
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

    fun requireValid(item: DefaultCapacityTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCapacityTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCapacityTargetValidator(): DefaultCapacityTargetValidator {
    return DefaultCapacityTargetValidator(
        mutableListOf<DefaultCapacityTargetRule>(
            DefaultCapacityTargetAllowedSourceRule(mutableListOf<String>()),
            DefaultCapacityTargetTitleLengthRule(3600),
            DefaultCapacityTargetCodeLengthRule(86400),
            DefaultCapacityTargetRouteCodeLengthRule(80),
            DefaultCapacityTargetRequiredRouteCodeRule("default capacity target needs a value"),
        ),
    )
}
