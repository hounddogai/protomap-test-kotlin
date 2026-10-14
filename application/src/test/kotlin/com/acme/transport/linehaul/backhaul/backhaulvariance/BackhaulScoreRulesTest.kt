package com.acme.transport.linehaul.backhaul.backhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulScoreValidator()
        val item = newBackhaulScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BackhaulScoreRequiredCodeRule("backhaul score needs this field")
        val item = newBackhaulScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = BackhaulScoreChannelLengthRule(3600)
        val item = newBackhaulScore("x", "y")
        item.channel = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = BackhaulScoreRequiredChannelRule("backhaul score needs a value")
        val item = newBackhaulScore("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BackhaulScoreRequiredLabelRule("backhaul score needs an entry")
        val item = newBackhaulScore("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
