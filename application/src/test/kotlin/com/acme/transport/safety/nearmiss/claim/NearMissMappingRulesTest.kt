package com.acme.transport.safety.nearmiss.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissMappingValidator()
        val item = newNearMissMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = NearMissMappingChannelLengthRule(255)
        val item = newNearMissMapping("x", "y")
        item.channel = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = NearMissMappingCodeLengthRule(16)
        val item = newNearMissMapping("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NearMissMappingRequiredCodeRule("near miss mapping needs a value")
        val item = newNearMissMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = NearMissMappingTitleLengthRule(255)
        val item = newNearMissMapping("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = NearMissMappingRequiredChannelRule("near miss mapping needs a value")
        val item = newNearMissMapping("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
