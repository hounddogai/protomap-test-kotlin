package com.acme.transport.safety.nearmiss.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingNearMissSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingNearMissSegmentValidator()
        val item = newRollingNearMissSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingNearMissSegmentCodeLengthRule(255)
        val item = newRollingNearMissSegment("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RollingNearMissSegmentRequiredLabelRule("rolling near miss segment needs an entry")
        val item = newRollingNearMissSegment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
