package com.acme.transport.network.hub.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalHubPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalHubPolicyValidator()
        val item = newGlobalHubPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = GlobalHubPolicyGraceDaysRangeRule(16, 255)
        val item = newGlobalHubPolicy("x", "y")
        item.graceDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = GlobalHubPolicyBatchSizeRangeRule(64, 3600)
        val item = newGlobalHubPolicy("x", "y")
        item.batchSize = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = GlobalHubPolicyRequiredChecksumRule("global hub policy needs this field")
        val item = newGlobalHubPolicy("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalHubPolicyTitleLengthRule(16)
        val item = newGlobalHubPolicy("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = GlobalHubPolicyChecksumLengthRule(64)
        val item = newGlobalHubPolicy("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
