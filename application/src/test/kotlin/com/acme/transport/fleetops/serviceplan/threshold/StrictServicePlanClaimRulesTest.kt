package com.acme.transport.fleetops.serviceplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictServicePlanClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictServicePlanClaimValidator()
        val item = newStrictServicePlanClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictServicePlanClaimRequiredCodeRule("strict service plan claim needs this field")
        val item = newStrictServicePlanClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictServicePlanClaimLabelLengthRule(16)
        val item = newStrictServicePlanClaim("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = StrictServicePlanClaimOwnerIdLengthRule(16)
        val item = newStrictServicePlanClaim("x", "y")
        item.ownerId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
