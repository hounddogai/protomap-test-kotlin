package com.acme.transport.network.servicearea.areaexception

/** A rule a capped service area mapping must pass. Check returns its problems, or none. */
interface CappedServiceAreaMappingRule {
    fun check(item: CappedServiceAreaMapping): MutableList<String>
}

data class CappedServiceAreaMappingDescriptionLengthRule(var maxLength: Int) : CappedServiceAreaMappingRule {
    override fun check(item: CappedServiceAreaMapping): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedServiceAreaMappingRequiredCodeRule(var message: String) : CappedServiceAreaMappingRule {
    override fun check(item: CappedServiceAreaMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedServiceAreaMappingRequiredContactEmailRule(
    var message: String,
) : CappedServiceAreaMappingRule {
    override fun check(item: CappedServiceAreaMapping): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedServiceAreaMappingCodePrefixRule(var prefix: String) : CappedServiceAreaMappingRule {
    override fun check(item: CappedServiceAreaMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedServiceAreaMappingRequiredLabelRule(var message: String) : CappedServiceAreaMappingRule {
    override fun check(item: CappedServiceAreaMapping): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedServiceAreaMappingContactEmailLengthRule(var maxLength: Int) : CappedServiceAreaMappingRule {
    override fun check(item: CappedServiceAreaMapping): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped service area mapping. */
data class CappedServiceAreaMappingValidator(var rules: MutableList<CappedServiceAreaMappingRule>) {
    fun validate(item: CappedServiceAreaMapping): MutableList<String> {
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

    fun requireValid(item: CappedServiceAreaMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedServiceAreaMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedServiceAreaMappingValidator(): CappedServiceAreaMappingValidator {
    return CappedServiceAreaMappingValidator(
        mutableListOf<CappedServiceAreaMappingRule>(
            CappedServiceAreaMappingDescriptionLengthRule(16),
            CappedServiceAreaMappingRequiredCodeRule("capped service area mapping needs this field"),
            CappedServiceAreaMappingRequiredContactEmailRule("capped service area mapping needs a value"),
            CappedServiceAreaMappingCodePrefixRule("REF"),
            CappedServiceAreaMappingRequiredLabelRule("capped service area mapping needs a value"),
            CappedServiceAreaMappingContactEmailLengthRule(3600),
        ),
    )
}
