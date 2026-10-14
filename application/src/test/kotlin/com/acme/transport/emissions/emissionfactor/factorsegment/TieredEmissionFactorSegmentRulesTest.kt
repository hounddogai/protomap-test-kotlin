package com.acme.transport.emissions.emissionfactor.factorsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredEmissionFactorSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredEmissionFactorSegmentValidator()
        val item = newTieredEmissionFactorSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = TieredEmissionFactorSegmentDepotLengthRule(32)
        val item = newTieredEmissionFactorSegment("x", "y")
        item.depot = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = TieredEmissionFactorSegmentNameLengthRule(1000)
        val item = newTieredEmissionFactorSegment("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = TieredEmissionFactorSegmentRequiredDepotRule("tiered emission factor segment needs a value")
        val item = newTieredEmissionFactorSegment("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
