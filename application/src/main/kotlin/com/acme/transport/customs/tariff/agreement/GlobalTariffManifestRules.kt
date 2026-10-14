package com.acme.transport.customs.tariff.agreement

import com.acme.transport.kit.text.normalize

/** A rule a global tariff manifest must pass. Check returns its problems, or none. */
interface GlobalTariffManifestRule {
    fun check(item: GlobalTariffManifest): MutableList<String>
}

data class GlobalTariffManifestRequiredNameRule(var message: String) : GlobalTariffManifestRule {
    override fun check(item: GlobalTariffManifest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTariffManifestRequiredDriverIdRule(var message: String) : GlobalTariffManifestRule {
    override fun check(item: GlobalTariffManifest): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTariffManifestAllowedCodeRule(var allowed: MutableList<String>) : GlobalTariffManifestRule {
    override fun check(item: GlobalTariffManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global tariff manifest. */
data class GlobalTariffManifestValidator(var rules: MutableList<GlobalTariffManifestRule>) {
    fun validate(item: GlobalTariffManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalTariffManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTariffManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTariffManifestValidator(): GlobalTariffManifestValidator {
    return GlobalTariffManifestValidator(
        mutableListOf<GlobalTariffManifestRule>(
            GlobalTariffManifestRequiredNameRule("global tariff manifest needs this field"),
            GlobalTariffManifestRequiredDriverIdRule("global tariff manifest needs this field"),
            GlobalTariffManifestAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
