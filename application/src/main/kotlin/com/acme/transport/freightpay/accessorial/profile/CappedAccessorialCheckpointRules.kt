package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.kit.text.normalize

/** A rule a capped accessorial checkpoint must pass. Check returns its problems, or none. */
interface CappedAccessorialCheckpointRule {
    fun check(item: CappedAccessorialCheckpoint): MutableList<String>
}

data class CappedAccessorialCheckpointRequiredCodeRule(var message: String) : CappedAccessorialCheckpointRule {
    override fun check(item: CappedAccessorialCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedAccessorialCheckpointAllowedCarrierRule(
    var allowed: MutableList<String>,
) : CappedAccessorialCheckpointRule {
    override fun check(item: CappedAccessorialCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class CappedAccessorialCheckpointCodeLengthRule(var maxLength: Int) : CappedAccessorialCheckpointRule {
    override fun check(item: CappedAccessorialCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedAccessorialCheckpointCodePrefixRule(var prefix: String) : CappedAccessorialCheckpointRule {
    override fun check(item: CappedAccessorialCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedAccessorialCheckpointNameLengthRule(var maxLength: Int) : CappedAccessorialCheckpointRule {
    override fun check(item: CappedAccessorialCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedAccessorialCheckpointTitleLengthRule(var maxLength: Int) : CappedAccessorialCheckpointRule {
    override fun check(item: CappedAccessorialCheckpoint): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped accessorial checkpoint. */
data class CappedAccessorialCheckpointValidator(var rules: MutableList<CappedAccessorialCheckpointRule>) {
    fun validate(item: CappedAccessorialCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedAccessorialCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedAccessorialCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedAccessorialCheckpointValidator(): CappedAccessorialCheckpointValidator {
    return CappedAccessorialCheckpointValidator(
        mutableListOf<CappedAccessorialCheckpointRule>(
            CappedAccessorialCheckpointRequiredCodeRule("capped accessorial checkpoint needs a value"),
            CappedAccessorialCheckpointAllowedCarrierRule(mutableListOf<String>()),
            CappedAccessorialCheckpointCodeLengthRule(120),
            CappedAccessorialCheckpointCodePrefixRule("REF"),
            CappedAccessorialCheckpointNameLengthRule(32),
            CappedAccessorialCheckpointTitleLengthRule(80),
        ),
    )
}
