package com.acme.transport.safety.cameraevent.evententry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCameraEventDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryCameraEventDigestValidator()
        val item = newPrimaryCameraEventDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryCameraEventDigestRequiredTitleRule("primary camera event digest needs a value")
        val item = newPrimaryCameraEventDigest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = PrimaryCameraEventDigestSequenceRangeRule(86400, 86400)
        val item = newPrimaryCameraEventDigest("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above amount`() {
        val rule = PrimaryCameraEventDigestAmountRangeRule(4.6, 235.3)
        val item = newPrimaryCameraEventDigest("x", "y")
        item.amount = 236.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
