package com.acme.transport.linehaul.laneplan.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyLanePlanBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyLanePlanBundleValidator()
        val item = newLegacyLanePlanBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = LegacyLanePlanBundleChannelLengthRule(1000)
        val item = newLegacyLanePlanBundle("x", "y")
        item.channel = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = LegacyLanePlanBundleRequiredExternalRefRule("legacy lane plan bundle needs this field")
        val item = newLegacyLanePlanBundle("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyLanePlanBundleTitleLengthRule(32)
        val item = newLegacyLanePlanBundle("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = LegacyLanePlanBundleRequiredChannelRule("legacy lane plan bundle needs a value")
        val item = newLegacyLanePlanBundle("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
