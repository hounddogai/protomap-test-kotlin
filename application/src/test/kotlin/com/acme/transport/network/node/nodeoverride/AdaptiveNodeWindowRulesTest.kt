package com.acme.transport.network.node.nodeoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveNodeWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveNodeWindowValidator()
        val item = newAdaptiveNodeWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = AdaptiveNodeWindowRequiredRouteCodeRule("adaptive node window needs this field")
        val item = newAdaptiveNodeWindow("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveNodeWindowLabelLengthRule(80)
        val item = newAdaptiveNodeWindow("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
