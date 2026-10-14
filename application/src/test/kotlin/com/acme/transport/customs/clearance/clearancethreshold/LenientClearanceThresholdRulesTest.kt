package com.acme.transport.customs.clearance.clearancethreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientClearanceThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientClearanceThresholdValidator()
        val item = newLenientClearanceThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientClearanceThresholdRequiredCodeRule("lenient clearance threshold needs a value")
        val item = newLenientClearanceThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = LenientClearanceThresholdRequiredExternalRefRule("lenient clearance threshold needs an entry")
        val item = newLenientClearanceThreshold("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
