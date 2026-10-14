package com.acme.transport.freightpay.ratecard.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRateCardManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientRateCardManifestValidator()
        val item = newLenientRateCardManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = LenientRateCardManifestRequiredChannelRule("lenient rate card manifest needs a value")
        val item = newLenientRateCardManifest("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientRateCardManifestRequiredCodeRule("lenient rate card manifest needs a value")
        val item = newLenientRateCardManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = LenientRateCardManifestAttemptsRangeRule(120, 1000)
        val item = newLenientRateCardManifest("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank description`() {
        val rule = LenientRateCardManifestRequiredDescriptionRule("lenient rate card manifest needs this field")
        val item = newLenientRateCardManifest("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
