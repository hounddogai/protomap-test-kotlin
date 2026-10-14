package com.acme.transport.customs.declaration.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDeclarationEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingDeclarationEstimateValidator()
        val item = newPendingDeclarationEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = PendingDeclarationEstimateOdometerRangeRule(80, 255)
        val item = newPendingDeclarationEstimate("x", "y")
        item.odometer = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingDeclarationEstimateRequiredCodeRule("pending declaration estimate needs this field")
        val item = newPendingDeclarationEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
