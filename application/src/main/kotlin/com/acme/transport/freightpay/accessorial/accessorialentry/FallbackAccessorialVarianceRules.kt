package com.acme.transport.freightpay.accessorial.accessorialentry

/** A rule a fallback accessorial variance must pass. Check returns its problems, or none. */
interface FallbackAccessorialVarianceRule {
    fun check(item: FallbackAccessorialVariance): MutableList<String>
}

data class FallbackAccessorialVarianceRequiredLabelRule(var message: String) : FallbackAccessorialVarianceRule {
    override fun check(item: FallbackAccessorialVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAccessorialVarianceCodePrefixRule(var prefix: String) : FallbackAccessorialVarianceRule {
    override fun check(item: FallbackAccessorialVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAccessorialVarianceContactEmailLengthRule(
    var maxLength: Int,
) : FallbackAccessorialVarianceRule {
    override fun check(item: FallbackAccessorialVariance): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAccessorialVarianceRequiredCodeRule(var message: String) : FallbackAccessorialVarianceRule {
    override fun check(item: FallbackAccessorialVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAccessorialVarianceRequiredContactEmailRule(
    var message: String,
) : FallbackAccessorialVarianceRule {
    override fun check(item: FallbackAccessorialVariance): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback accessorial variance. */
data class FallbackAccessorialVarianceValidator(var rules: MutableList<FallbackAccessorialVarianceRule>) {
    fun validate(item: FallbackAccessorialVariance): MutableList<String> {
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

    fun requireValid(item: FallbackAccessorialVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackAccessorialVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackAccessorialVarianceValidator(): FallbackAccessorialVarianceValidator {
    return FallbackAccessorialVarianceValidator(
        mutableListOf<FallbackAccessorialVarianceRule>(
            FallbackAccessorialVarianceRequiredLabelRule("fallback accessorial variance needs a value"),
            FallbackAccessorialVarianceCodePrefixRule("ACM"),
            FallbackAccessorialVarianceContactEmailLengthRule(86400),
            FallbackAccessorialVarianceRequiredCodeRule("fallback accessorial variance needs this field"),
            FallbackAccessorialVarianceRequiredContactEmailRule("fallback accessorial variance needs an entry"),
        ),
    )
}
