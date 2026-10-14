package com.acme.transport.emissions.idling.manifest

/** A rule a pending idling manifest must pass. Check returns its problems, or none. */
interface PendingIdlingManifestRule {
    fun check(item: PendingIdlingManifest): MutableList<String>
}

data class PendingIdlingManifestCodePrefixRule(var prefix: String) : PendingIdlingManifestRule {
    override fun check(item: PendingIdlingManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingManifestCodeLengthRule(var maxLength: Int) : PendingIdlingManifestRule {
    override fun check(item: PendingIdlingManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingManifestLabelLengthRule(var maxLength: Int) : PendingIdlingManifestRule {
    override fun check(item: PendingIdlingManifest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingManifestRequiredCodeRule(var message: String) : PendingIdlingManifestRule {
    override fun check(item: PendingIdlingManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingManifestCreatedAtRangeRule(var min: Int, var max: Int) : PendingIdlingManifestRule {
    override fun check(item: PendingIdlingManifest): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending idling manifest. */
data class PendingIdlingManifestValidator(var rules: MutableList<PendingIdlingManifestRule>) {
    fun validate(item: PendingIdlingManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingIdlingManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingIdlingManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingIdlingManifestValidator(): PendingIdlingManifestValidator {
    return PendingIdlingManifestValidator(
        mutableListOf<PendingIdlingManifestRule>(
            PendingIdlingManifestCodePrefixRule("REF"),
            PendingIdlingManifestCodeLengthRule(86400),
            PendingIdlingManifestLabelLengthRule(120),
            PendingIdlingManifestRequiredCodeRule("pending idling manifest needs an entry"),
            PendingIdlingManifestCreatedAtRangeRule(16, 3600),
        ),
    )
}
