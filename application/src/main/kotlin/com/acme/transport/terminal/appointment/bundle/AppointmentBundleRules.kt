package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.text.normalize

/** A rule a appointment bundle must pass. Check returns its problems, or none. */
interface AppointmentBundleRule {
    fun check(item: AppointmentBundle): MutableList<String>
}

data class AppointmentBundleRequiredLabelRule(var message: String) : AppointmentBundleRule {
    override fun check(item: AppointmentBundle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentBundleCodePrefixRule(var prefix: String) : AppointmentBundleRule {
    override fun check(item: AppointmentBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentBundleRequiredCodeRule(var message: String) : AppointmentBundleRule {
    override fun check(item: AppointmentBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentBundleAllowedRouteCodeRule(var allowed: MutableList<String>) : AppointmentBundleRule {
    override fun check(item: AppointmentBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is stale")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentBundleActiveGuardRule(var limit: Int) : AppointmentBundleRule {
    override fun check(item: AppointmentBundle): MutableList<String> {
        if (item.active && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment bundle. */
data class AppointmentBundleValidator(var rules: MutableList<AppointmentBundleRule>) {
    fun validate(item: AppointmentBundle): MutableList<String> {
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

    fun requireValid(item: AppointmentBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentBundleValidator(): AppointmentBundleValidator {
    return AppointmentBundleValidator(
        mutableListOf<AppointmentBundleRule>(
            AppointmentBundleRequiredLabelRule("appointment bundle needs a value"),
            AppointmentBundleCodePrefixRule("OPS"),
            AppointmentBundleRequiredCodeRule("appointment bundle needs an entry"),
            AppointmentBundleAllowedRouteCodeRule(mutableListOf<String>("tiered", "manual", "pending")),
            AppointmentBundleActiveGuardRule(80),
        ),
    )
}
