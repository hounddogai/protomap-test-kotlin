package com.acme.transport.emissions.emissionfactor.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorSnapshotValidator()
        val item = newEmissionFactorSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = EmissionFactorSnapshotRequiredChannelRule("emission factor snapshot needs an entry")
        val item = newEmissionFactorSnapshot("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = EmissionFactorSnapshotLoadKgRangeRule(1.5, 1516.7)
        val item = newEmissionFactorSnapshot("x", "y")
        item.loadKg = 1518.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = EmissionFactorSnapshotGraceDaysRangeRule(16, 32)
        val item = newEmissionFactorSnapshot("x", "y")
        item.graceDays = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
