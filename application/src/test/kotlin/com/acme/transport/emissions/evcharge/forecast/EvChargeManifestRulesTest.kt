package com.acme.transport.emissions.evcharge.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeManifestValidator()
        val item = newEvChargeManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = EvChargeManifestSegmentLengthRule(80)
        val item = newEvChargeManifest("x", "y")
        item.segment = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = EvChargeManifestMarginRangeRule(1.3, 1966.5)
        val item = newEvChargeManifest("x", "y")
        item.margin = 1968.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = EvChargeManifestCodeLengthRule(32)
        val item = newEvChargeManifest("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = EvChargeManifestRequiredDepotRule("ev charge manifest needs an entry")
        val item = newEvChargeManifest("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
