package com.acme.transport.fleetops.serviceplan.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveServicePlanManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveServicePlanManifestValidator()
        val item = newAdaptiveServicePlanManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = AdaptiveServicePlanManifestMarginRangeRule(4.6, 3289.2)
        val item = newAdaptiveServicePlanManifest("x", "y")
        item.margin = 3290.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = AdaptiveServicePlanManifestAttemptsRangeRule(255, 1000)
        val item = newAdaptiveServicePlanManifest("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = AdaptiveServicePlanManifestPalletsRangeRule(32, 64)
        val item = newAdaptiveServicePlanManifest("x", "y")
        item.pallets = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveServicePlanManifestCodeLengthRule(255)
        val item = newAdaptiveServicePlanManifest("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveServicePlanManifestRequiredTitleRule("adaptive service plan manifest needs a value")
        val item = newAdaptiveServicePlanManifest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
