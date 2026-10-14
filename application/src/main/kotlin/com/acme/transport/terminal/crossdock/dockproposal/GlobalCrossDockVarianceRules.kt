package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.text.normalize

/** A rule a global cross dock variance must pass. Check returns its problems, or none. */
interface GlobalCrossDockVarianceRule {
    fun check(item: GlobalCrossDockVariance): MutableList<String>
}

data class GlobalCrossDockVarianceCodeLengthRule(var maxLength: Int) : GlobalCrossDockVarianceRule {
    override fun check(item: GlobalCrossDockVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCrossDockVarianceRequiredCodeRule(var message: String) : GlobalCrossDockVarianceRule {
    override fun check(item: GlobalCrossDockVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalCrossDockVarianceRequiredDepotRule(var message: String) : GlobalCrossDockVarianceRule {
    override fun check(item: GlobalCrossDockVariance): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalCrossDockVarianceAllowedCodeRule(
    var allowed: MutableList<String>,
) : GlobalCrossDockVarianceRule {
    override fun check(item: GlobalCrossDockVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCrossDockVarianceTitleLengthRule(var maxLength: Int) : GlobalCrossDockVarianceRule {
    override fun check(item: GlobalCrossDockVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global cross dock variance. */
data class GlobalCrossDockVarianceValidator(var rules: MutableList<GlobalCrossDockVarianceRule>) {
    fun validate(item: GlobalCrossDockVariance): MutableList<String> {
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

    fun requireValid(item: GlobalCrossDockVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCrossDockVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCrossDockVarianceValidator(): GlobalCrossDockVarianceValidator {
    return GlobalCrossDockVarianceValidator(
        mutableListOf<GlobalCrossDockVarianceRule>(
            GlobalCrossDockVarianceCodeLengthRule(3600),
            GlobalCrossDockVarianceRequiredCodeRule("global cross dock variance needs this field"),
            GlobalCrossDockVarianceRequiredDepotRule("global cross dock variance needs an entry"),
            GlobalCrossDockVarianceAllowedCodeRule(mutableListOf<String>("batched", "fallback", "lenient")),
            GlobalCrossDockVarianceTitleLengthRule(86400),
        ),
    )
}
