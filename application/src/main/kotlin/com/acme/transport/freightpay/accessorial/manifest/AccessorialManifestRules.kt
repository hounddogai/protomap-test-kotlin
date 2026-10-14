package com.acme.transport.freightpay.accessorial.manifest

/** A rule a accessorial manifest must pass. Check returns its problems, or none. */
interface AccessorialManifestRule {
    fun check(item: AccessorialManifest): MutableList<String>
}

data class AccessorialManifestTitleLengthRule(var maxLength: Int) : AccessorialManifestRule {
    override fun check(item: AccessorialManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialManifestCodeLengthRule(var maxLength: Int) : AccessorialManifestRule {
    override fun check(item: AccessorialManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialManifestSummaryLengthRule(var maxLength: Int) : AccessorialManifestRule {
    override fun check(item: AccessorialManifest): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialManifestRequiredDriverIdRule(var message: String) : AccessorialManifestRule {
    override fun check(item: AccessorialManifest): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial manifest. */
data class AccessorialManifestValidator(var rules: MutableList<AccessorialManifestRule>) {
    fun validate(item: AccessorialManifest): MutableList<String> {
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

    fun requireValid(item: AccessorialManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialManifestValidator(): AccessorialManifestValidator {
    return AccessorialManifestValidator(
        mutableListOf<AccessorialManifestRule>(
            AccessorialManifestTitleLengthRule(86400),
            AccessorialManifestCodeLengthRule(3600),
            AccessorialManifestSummaryLengthRule(1000),
            AccessorialManifestRequiredDriverIdRule("accessorial manifest needs this field"),
        ),
    )
}
