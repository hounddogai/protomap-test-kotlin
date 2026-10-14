package com.acme.transport.terminal.crossdock.dockestimate

/** A rule a cross dock policy must pass. Check returns its problems, or none. */
interface CrossDockPolicyRule {
    fun check(item: CrossDockPolicy): MutableList<String>
}

data class CrossDockPolicyTrailerIdLengthRule(var maxLength: Int) : CrossDockPolicyRule {
    override fun check(item: CrossDockPolicy): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockPolicyCodeLengthRule(var maxLength: Int) : CrossDockPolicyRule {
    override fun check(item: CrossDockPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockPolicyCodePrefixRule(var prefix: String) : CrossDockPolicyRule {
    override fun check(item: CrossDockPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockPolicyRequiredTrailerIdRule(var message: String) : CrossDockPolicyRule {
    override fun check(item: CrossDockPolicy): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockPolicyTitleLengthRule(var maxLength: Int) : CrossDockPolicyRule {
    override fun check(item: CrossDockPolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockPolicyFloorValueRangeRule(var min: Double, var max: Double) : CrossDockPolicyRule {
    override fun check(item: CrossDockPolicy): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock policy. */
data class CrossDockPolicyValidator(var rules: MutableList<CrossDockPolicyRule>) {
    fun validate(item: CrossDockPolicy): MutableList<String> {
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

    fun requireValid(item: CrossDockPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockPolicyValidator(): CrossDockPolicyValidator {
    return CrossDockPolicyValidator(
        mutableListOf<CrossDockPolicyRule>(
            CrossDockPolicyTrailerIdLengthRule(86400),
            CrossDockPolicyCodeLengthRule(3600),
            CrossDockPolicyCodePrefixRule("OPS"),
            CrossDockPolicyRequiredTrailerIdRule("cross dock policy needs this field"),
            CrossDockPolicyTitleLengthRule(1000),
            CrossDockPolicyFloorValueRangeRule(3.7, 1025.3),
        ),
    )
}
