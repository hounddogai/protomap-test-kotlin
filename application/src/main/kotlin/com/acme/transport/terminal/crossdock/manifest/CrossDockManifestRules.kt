package com.acme.transport.terminal.crossdock.manifest

/** A rule a cross dock manifest must pass. Check returns its problems, or none. */
interface CrossDockManifestRule {
    fun check(item: CrossDockManifest): MutableList<String>
}

data class CrossDockManifestRequiredDepotRule(var message: String) : CrossDockManifestRule {
    override fun check(item: CrossDockManifest): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockManifestRequiredCodeRule(var message: String) : CrossDockManifestRule {
    override fun check(item: CrossDockManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockManifestTitleLengthRule(var maxLength: Int) : CrossDockManifestRule {
    override fun check(item: CrossDockManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock manifest. */
data class CrossDockManifestValidator(var rules: MutableList<CrossDockManifestRule>) {
    fun validate(item: CrossDockManifest): MutableList<String> {
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

    fun requireValid(item: CrossDockManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockManifestValidator(): CrossDockManifestValidator {
    return CrossDockManifestValidator(
        mutableListOf<CrossDockManifestRule>(
            CrossDockManifestRequiredDepotRule("cross dock manifest needs this field"),
            CrossDockManifestRequiredCodeRule("cross dock manifest needs an entry"),
            CrossDockManifestTitleLengthRule(32),
        ),
    )
}
