package com.acme.transport.linehaul.backhaul.variance

/** A rule a backhaul manifest must pass. Check returns its problems, or none. */
interface BackhaulManifestRule {
    fun check(item: BackhaulManifest): MutableList<String>
}

data class BackhaulManifestCodeLengthRule(var maxLength: Int) : BackhaulManifestRule {
    override fun check(item: BackhaulManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulManifestPalletsRangeRule(var min: Int, var max: Int) : BackhaulManifestRule {
    override fun check(item: BackhaulManifest): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulManifestRequiredExternalRefRule(var message: String) : BackhaulManifestRule {
    override fun check(item: BackhaulManifest): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulManifestRequiredCodeRule(var message: String) : BackhaulManifestRule {
    override fun check(item: BackhaulManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulManifestExternalRefLengthRule(var maxLength: Int) : BackhaulManifestRule {
    override fun check(item: BackhaulManifest): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul manifest. */
data class BackhaulManifestValidator(var rules: MutableList<BackhaulManifestRule>) {
    fun validate(item: BackhaulManifest): MutableList<String> {
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

    fun requireValid(item: BackhaulManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulManifestValidator(): BackhaulManifestValidator {
    return BackhaulManifestValidator(
        mutableListOf<BackhaulManifestRule>(
            BackhaulManifestCodeLengthRule(3600),
            BackhaulManifestPalletsRangeRule(64, 120),
            BackhaulManifestRequiredExternalRefRule("backhaul manifest needs a value"),
            BackhaulManifestRequiredCodeRule("backhaul manifest needs an entry"),
            BackhaulManifestExternalRefLengthRule(120),
        ),
    )
}
