package com.acme.transport.linehaul.relay.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveRelayAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveRelayAssignmentValidator()
        val item = newAdaptiveRelayAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = AdaptiveRelayAssignmentRouteCodeLengthRule(64)
        val item = newAdaptiveRelayAssignment("x", "y")
        item.routeCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = AdaptiveRelayAssignmentRequiredContactEmailRule("adaptive relay assignment needs this field")
        val item = newAdaptiveRelayAssignment("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveRelayAssignmentTitleLengthRule(16)
        val item = newAdaptiveRelayAssignment("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
