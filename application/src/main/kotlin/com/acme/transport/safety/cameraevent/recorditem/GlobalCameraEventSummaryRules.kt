package com.acme.transport.safety.cameraevent.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a global camera event summary must pass. Check returns its problems, or none. */
interface GlobalCameraEventSummaryRule {
    fun check(item: GlobalCameraEventSummary): MutableList<String>
}

data class GlobalCameraEventSummaryDockLengthRule(var maxLength: Int) : GlobalCameraEventSummaryRule {
    override fun check(item: GlobalCameraEventSummary): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCameraEventSummaryCodePrefixRule(var prefix: String) : GlobalCameraEventSummaryRule {
    override fun check(item: GlobalCameraEventSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCameraEventSummaryLabelLengthRule(var maxLength: Int) : GlobalCameraEventSummaryRule {
    override fun check(item: GlobalCameraEventSummary): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCameraEventSummaryAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : GlobalCameraEventSummaryRule {
    override fun check(item: GlobalCameraEventSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCameraEventSummaryRequiredDockRule(var message: String) : GlobalCameraEventSummaryRule {
    override fun check(item: GlobalCameraEventSummary): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalCameraEventSummaryLimitRangeRule(var min: Int, var max: Int) : GlobalCameraEventSummaryRule {
    override fun check(item: GlobalCameraEventSummary): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global camera event summary. */
data class GlobalCameraEventSummaryValidator(var rules: MutableList<GlobalCameraEventSummaryRule>) {
    fun validate(item: GlobalCameraEventSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalCameraEventSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCameraEventSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCameraEventSummaryValidator(): GlobalCameraEventSummaryValidator {
    return GlobalCameraEventSummaryValidator(
        mutableListOf<GlobalCameraEventSummaryRule>(
            GlobalCameraEventSummaryDockLengthRule(1000),
            GlobalCameraEventSummaryCodePrefixRule("OPS"),
            GlobalCameraEventSummaryLabelLengthRule(80),
            GlobalCameraEventSummaryAllowedOwnerIdRule(mutableListOf<String>("weighted")),
            GlobalCameraEventSummaryRequiredDockRule("global camera event summary needs a value"),
            GlobalCameraEventSummaryLimitRangeRule(32, 86400),
        ),
    )
}
