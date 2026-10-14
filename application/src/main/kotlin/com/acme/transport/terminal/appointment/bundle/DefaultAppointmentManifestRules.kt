package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.text.normalize

/** A rule a default appointment manifest must pass. Check returns its problems, or none. */
interface DefaultAppointmentManifestRule {
    fun check(item: DefaultAppointmentManifest): MutableList<String>
}

data class DefaultAppointmentManifestRequiredCategoryRule(
    var message: String,
) : DefaultAppointmentManifestRule {
    override fun check(item: DefaultAppointmentManifest): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultAppointmentManifestAllowedCategoryRule(
    var allowed: MutableList<String>,
) : DefaultAppointmentManifestRule {
    override fun check(item: DefaultAppointmentManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is missing")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAppointmentManifestRequiredTitleRule(var message: String) : DefaultAppointmentManifestRule {
    override fun check(item: DefaultAppointmentManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultAppointmentManifestCostRangeRule(
    var min: Double,
    var max: Double,
) : DefaultAppointmentManifestRule {
    override fun check(item: DefaultAppointmentManifest): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class DefaultAppointmentManifestRequiredCodeRule(var message: String) : DefaultAppointmentManifestRule {
    override fun check(item: DefaultAppointmentManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default appointment manifest. */
data class DefaultAppointmentManifestValidator(var rules: MutableList<DefaultAppointmentManifestRule>) {
    fun validate(item: DefaultAppointmentManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultAppointmentManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultAppointmentManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultAppointmentManifestValidator(): DefaultAppointmentManifestValidator {
    return DefaultAppointmentManifestValidator(
        mutableListOf<DefaultAppointmentManifestRule>(
            DefaultAppointmentManifestRequiredCategoryRule("default appointment manifest needs an entry"),
            DefaultAppointmentManifestAllowedCategoryRule(mutableListOf<String>("default", "rolling")),
            DefaultAppointmentManifestRequiredTitleRule("default appointment manifest needs an entry"),
            DefaultAppointmentManifestCostRangeRule(3.5, 3487.1),
            DefaultAppointmentManifestRequiredCodeRule("default appointment manifest needs an entry"),
        ),
    )
}
