package com.acme.transport.fleetops.leasereturn.returnpreference

import com.acme.transport.kit.text.normalize

/** A rule a global lease return rollup must pass. Check returns its problems, or none. */
interface GlobalLeaseReturnRollupRule {
    fun check(item: GlobalLeaseReturnRollup): MutableList<String>
}

data class GlobalLeaseReturnRollupRequiredExternalRefRule(var message: String) : GlobalLeaseReturnRollupRule {
    override fun check(item: GlobalLeaseReturnRollup): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalLeaseReturnRollupRequiredLabelRule(var message: String) : GlobalLeaseReturnRollupRule {
    override fun check(item: GlobalLeaseReturnRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalLeaseReturnRollupAllowedUnitRule(
    var allowed: MutableList<String>,
) : GlobalLeaseReturnRollupRule {
    override fun check(item: GlobalLeaseReturnRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global lease return rollup. */
data class GlobalLeaseReturnRollupValidator(var rules: MutableList<GlobalLeaseReturnRollupRule>) {
    fun validate(item: GlobalLeaseReturnRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalLeaseReturnRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalLeaseReturnRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalLeaseReturnRollupValidator(): GlobalLeaseReturnRollupValidator {
    return GlobalLeaseReturnRollupValidator(
        mutableListOf<GlobalLeaseReturnRollupRule>(
            GlobalLeaseReturnRollupRequiredExternalRefRule("global lease return rollup needs a value"),
            GlobalLeaseReturnRollupRequiredLabelRule("global lease return rollup needs this field"),
            GlobalLeaseReturnRollupAllowedUnitRule(mutableListOf<String>()),
        ),
    )
}
