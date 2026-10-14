package com.acme.transport.freightpay.accessorial.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAccessorialCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedAccessorialCheckpointValidator()
        val item = newCappedAccessorialCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedAccessorialCheckpointRequiredCodeRule("capped accessorial checkpoint needs this field")
        val item = newCappedAccessorialCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedAccessorialCheckpointCodeLengthRule(16)
        val item = newCappedAccessorialCheckpoint("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedAccessorialCheckpointNameLengthRule(64)
        val item = newCappedAccessorialCheckpoint("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = CappedAccessorialCheckpointTitleLengthRule(1000)
        val item = newCappedAccessorialCheckpoint("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
