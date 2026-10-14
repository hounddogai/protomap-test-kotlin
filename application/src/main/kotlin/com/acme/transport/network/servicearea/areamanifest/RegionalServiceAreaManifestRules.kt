package com.acme.transport.network.servicearea.areamanifest

/** A rule a regional service area manifest must pass. Check returns its problems, or none. */
interface RegionalServiceAreaManifestRule {
    fun check(item: RegionalServiceAreaManifest): MutableList<String>
}

data class RegionalServiceAreaManifestCodePrefixRule(var prefix: String) : RegionalServiceAreaManifestRule {
    override fun check(item: RegionalServiceAreaManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalServiceAreaManifestCodeLengthRule(var maxLength: Int) : RegionalServiceAreaManifestRule {
    override fun check(item: RegionalServiceAreaManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalServiceAreaManifestRequiredTimezoneRule(
    var message: String,
) : RegionalServiceAreaManifestRule {
    override fun check(item: RegionalServiceAreaManifest): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalServiceAreaManifestNameLengthRule(var maxLength: Int) : RegionalServiceAreaManifestRule {
    override fun check(item: RegionalServiceAreaManifest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalServiceAreaManifestRequiredCodeRule(var message: String) : RegionalServiceAreaManifestRule {
    override fun check(item: RegionalServiceAreaManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalServiceAreaManifestPinnedGuardRule(var limit: Int) : RegionalServiceAreaManifestRule {
    override fun check(item: RegionalServiceAreaManifest): MutableList<String> {
        if (item.pinned && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional service area manifest. */
data class RegionalServiceAreaManifestValidator(var rules: MutableList<RegionalServiceAreaManifestRule>) {
    fun validate(item: RegionalServiceAreaManifest): MutableList<String> {
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

    fun requireValid(item: RegionalServiceAreaManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalServiceAreaManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalServiceAreaManifestValidator(): RegionalServiceAreaManifestValidator {
    return RegionalServiceAreaManifestValidator(
        mutableListOf<RegionalServiceAreaManifestRule>(
            RegionalServiceAreaManifestCodePrefixRule("ACM"),
            RegionalServiceAreaManifestCodeLengthRule(86400),
            RegionalServiceAreaManifestRequiredTimezoneRule("regional service area manifest needs a value"),
            RegionalServiceAreaManifestNameLengthRule(86400),
            RegionalServiceAreaManifestRequiredCodeRule("regional service area manifest needs a value"),
            RegionalServiceAreaManifestPinnedGuardRule(64),
        ),
    )
}
