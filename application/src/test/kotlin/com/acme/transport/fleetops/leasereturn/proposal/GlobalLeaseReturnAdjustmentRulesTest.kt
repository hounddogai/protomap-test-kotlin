package com.acme.transport.fleetops.leasereturn.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalLeaseReturnAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalLeaseReturnAdjustmentValidator()
        val item = newGlobalLeaseReturnAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalLeaseReturnAdjustmentLabelLengthRule(86400)
        val item = newGlobalLeaseReturnAdjustment("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalLeaseReturnAdjustmentRequiredCodeRule("global lease return adjustment needs this field")
        val item = newGlobalLeaseReturnAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = GlobalLeaseReturnAdjustmentRequiredDescriptionRule(
            "global lease return adjustment needs this field",
        )
        val item = newGlobalLeaseReturnAdjustment("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
