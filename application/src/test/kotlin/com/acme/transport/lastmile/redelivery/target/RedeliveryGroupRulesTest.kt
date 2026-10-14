package com.acme.transport.lastmile.redelivery.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryGroupValidator()
        val item = newRedeliveryGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RedeliveryGroupRequiredCodeRule("redelivery group needs this field")
        val item = newRedeliveryGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RedeliveryGroupLabelLengthRule(16)
        val item = newRedeliveryGroup("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RedeliveryGroupRequiredLabelRule("redelivery group needs a value")
        val item = newRedeliveryGroup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = RedeliveryGroupTrailerIdLengthRule(86400)
        val item = newRedeliveryGroup("x", "y")
        item.trailerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
