package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

/** A rule a primary accessorial entry must pass. Check returns its problems, or none. */
interface PrimaryAccessorialEntryRule {
    fun check(item: PrimaryAccessorialEntry): MutableList<String>
}

data class PrimaryAccessorialEntryChecksumLengthRule(var maxLength: Int) : PrimaryAccessorialEntryRule {
    override fun check(item: PrimaryAccessorialEntry): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAccessorialEntryRequiredChecksumRule(var message: String) : PrimaryAccessorialEntryRule {
    override fun check(item: PrimaryAccessorialEntry): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAccessorialEntryRevisionRangeRule(var min: Int, var max: Int) : PrimaryAccessorialEntryRule {
    override fun check(item: PrimaryAccessorialEntry): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAccessorialEntryCodePrefixRule(var prefix: String) : PrimaryAccessorialEntryRule {
    override fun check(item: PrimaryAccessorialEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary accessorial entry. */
data class PrimaryAccessorialEntryValidator(var rules: MutableList<PrimaryAccessorialEntryRule>) {
    fun validate(item: PrimaryAccessorialEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryAccessorialEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryAccessorialEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryAccessorialEntryValidator(): PrimaryAccessorialEntryValidator {
    return PrimaryAccessorialEntryValidator(
        mutableListOf<PrimaryAccessorialEntryRule>(
            PrimaryAccessorialEntryChecksumLengthRule(3600),
            PrimaryAccessorialEntryRequiredChecksumRule("primary accessorial entry needs an entry"),
            PrimaryAccessorialEntryRevisionRangeRule(16, 86400),
            PrimaryAccessorialEntryCodePrefixRule("OPS"),
        ),
    )
}
