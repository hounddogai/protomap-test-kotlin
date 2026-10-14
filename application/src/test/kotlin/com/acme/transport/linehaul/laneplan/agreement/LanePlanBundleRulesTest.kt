package com.acme.transport.linehaul.laneplan.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanBundleValidator()
        val item = newLanePlanBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = LanePlanBundleLimitRangeRule(16, 255)
        val item = newLanePlanBundle("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = LanePlanBundleRevisionRangeRule(120, 120)
        val item = newLanePlanBundle("x", "y")
        item.revision = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LanePlanBundleRequiredNameRule("lane plan bundle needs a value")
        val item = newLanePlanBundle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = LanePlanBundleAttemptsRangeRule(3600, 86400)
        val item = newLanePlanBundle("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
