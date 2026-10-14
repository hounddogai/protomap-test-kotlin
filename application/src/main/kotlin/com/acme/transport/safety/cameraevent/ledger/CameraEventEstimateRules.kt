package com.acme.transport.safety.cameraevent.ledger

/** A rule a camera event estimate must pass. Check returns its problems, or none. */
interface CameraEventEstimateRule {
    fun check(item: CameraEventEstimate): MutableList<String>
}

data class CameraEventEstimateExternalRefLengthRule(var maxLength: Int) : CameraEventEstimateRule {
    override fun check(item: CameraEventEstimate): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventEstimateSummaryLengthRule(var maxLength: Int) : CameraEventEstimateRule {
    override fun check(item: CameraEventEstimate): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventEstimateRequiredExternalRefRule(var message: String) : CameraEventEstimateRule {
    override fun check(item: CameraEventEstimate): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventEstimateRequiredCodeRule(var message: String) : CameraEventEstimateRule {
    override fun check(item: CameraEventEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event estimate. */
data class CameraEventEstimateValidator(var rules: MutableList<CameraEventEstimateRule>) {
    fun validate(item: CameraEventEstimate): MutableList<String> {
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

    fun requireValid(item: CameraEventEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventEstimateValidator(): CameraEventEstimateValidator {
    return CameraEventEstimateValidator(
        mutableListOf<CameraEventEstimateRule>(
            CameraEventEstimateExternalRefLengthRule(3600),
            CameraEventEstimateSummaryLengthRule(3600),
            CameraEventEstimateRequiredExternalRefRule("camera event estimate needs an entry"),
            CameraEventEstimateRequiredCodeRule("camera event estimate needs a value"),
        ),
    )
}
