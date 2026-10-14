package com.acme.transport.crewing.roster.rosterrule

/** A rule a capped roster manifest must pass. Check returns its problems, or none. */
interface CappedRosterManifestRule {
    fun check(item: CappedRosterManifest): MutableList<String>
}

data class CappedRosterManifestDriverIdLengthRule(var maxLength: Int) : CappedRosterManifestRule {
    override fun check(item: CappedRosterManifest): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRosterManifestRequiredDriverIdRule(var message: String) : CappedRosterManifestRule {
    override fun check(item: CappedRosterManifest): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedRosterManifestContactEmailLengthRule(var maxLength: Int) : CappedRosterManifestRule {
    override fun check(item: CappedRosterManifest): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped roster manifest. */
data class CappedRosterManifestValidator(var rules: MutableList<CappedRosterManifestRule>) {
    fun validate(item: CappedRosterManifest): MutableList<String> {
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

    fun requireValid(item: CappedRosterManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedRosterManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedRosterManifestValidator(): CappedRosterManifestValidator {
    return CappedRosterManifestValidator(
        mutableListOf<CappedRosterManifestRule>(
            CappedRosterManifestDriverIdLengthRule(86400),
            CappedRosterManifestRequiredDriverIdRule("capped roster manifest needs this field"),
            CappedRosterManifestContactEmailLengthRule(64),
        ),
    )
}
