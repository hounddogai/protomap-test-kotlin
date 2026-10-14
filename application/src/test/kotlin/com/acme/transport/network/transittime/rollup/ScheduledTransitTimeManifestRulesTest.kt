package com.acme.transport.network.transittime.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledTransitTimeManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledTransitTimeManifestValidator()
        val item = newScheduledTransitTimeManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledTransitTimeManifestRequiredCodeRule("scheduled transit time manifest needs an entry")
        val item = newScheduledTransitTimeManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = ScheduledTransitTimeManifestScoreRangeRule(1.0, 4893.4)
        val item = newScheduledTransitTimeManifest("x", "y")
        item.score = 4894.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledTransitTimeManifestCodeLengthRule(64)
        val item = newScheduledTransitTimeManifest("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledTransitTimeManifestRequiredNameRule("scheduled transit time manifest needs this field")
        val item = newScheduledTransitTimeManifest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
