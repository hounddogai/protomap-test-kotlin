package com.acme.transport.network.servicearea.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a global service area snapshot must pass. Check returns its problems, or none. */
interface GlobalServiceAreaSnapshotRule {
    fun check(item: GlobalServiceAreaSnapshot): MutableList<String>
}

data class GlobalServiceAreaSnapshotChecksumLengthRule(var maxLength: Int) : GlobalServiceAreaSnapshotRule {
    override fun check(item: GlobalServiceAreaSnapshot): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalServiceAreaSnapshotRequiredNameRule(var message: String) : GlobalServiceAreaSnapshotRule {
    override fun check(item: GlobalServiceAreaSnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalServiceAreaSnapshotRequiredCodeRule(var message: String) : GlobalServiceAreaSnapshotRule {
    override fun check(item: GlobalServiceAreaSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalServiceAreaSnapshotAllowedNameRule(
    var allowed: MutableList<String>,
) : GlobalServiceAreaSnapshotRule {
    override fun check(item: GlobalServiceAreaSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is expired")
        }
        return mutableListOf<String>()
    }
}

data class GlobalServiceAreaSnapshotNameLengthRule(var maxLength: Int) : GlobalServiceAreaSnapshotRule {
    override fun check(item: GlobalServiceAreaSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global service area snapshot. */
data class GlobalServiceAreaSnapshotValidator(var rules: MutableList<GlobalServiceAreaSnapshotRule>) {
    fun validate(item: GlobalServiceAreaSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalServiceAreaSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalServiceAreaSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalServiceAreaSnapshotValidator(): GlobalServiceAreaSnapshotValidator {
    return GlobalServiceAreaSnapshotValidator(
        mutableListOf<GlobalServiceAreaSnapshotRule>(
            GlobalServiceAreaSnapshotChecksumLengthRule(80),
            GlobalServiceAreaSnapshotRequiredNameRule("global service area snapshot needs this field"),
            GlobalServiceAreaSnapshotRequiredCodeRule("global service area snapshot needs this field"),
            GlobalServiceAreaSnapshotAllowedNameRule(mutableListOf<String>("tiered")),
            GlobalServiceAreaSnapshotNameLengthRule(64),
        ),
    )
}
