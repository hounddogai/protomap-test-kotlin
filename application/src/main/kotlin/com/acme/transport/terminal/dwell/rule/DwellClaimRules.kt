package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.text.normalize

/** A rule a dwell claim must pass. Check returns its problems, or none. */
interface DwellClaimRule {
    fun check(item: DwellClaim): MutableList<String>
}

data class DwellClaimPinnedGuardRule(var limit: Double) : DwellClaimRule {
    override fun check(item: DwellClaim): MutableList<String> {
        if (item.pinned && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class DwellClaimChecksumLengthRule(var maxLength: Int) : DwellClaimRule {
    override fun check(item: DwellClaim): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellClaimContactEmailLengthRule(var maxLength: Int) : DwellClaimRule {
    override fun check(item: DwellClaim): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellClaimAllowedTitleRule(var allowed: MutableList<String>) : DwellClaimRule {
    override fun check(item: DwellClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class DwellClaimRequiredChecksumRule(var message: String) : DwellClaimRule {
    override fun check(item: DwellClaim): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell claim. */
data class DwellClaimValidator(var rules: MutableList<DwellClaimRule>) {
    fun validate(item: DwellClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DwellClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellClaimValidator(): DwellClaimValidator {
    return DwellClaimValidator(
        mutableListOf<DwellClaimRule>(
            DwellClaimPinnedGuardRule(4.4),
            DwellClaimChecksumLengthRule(32),
            DwellClaimContactEmailLengthRule(16),
            DwellClaimAllowedTitleRule(mutableListOf<String>("seasonal")),
            DwellClaimRequiredChecksumRule("dwell claim needs a value"),
        ),
    )
}
