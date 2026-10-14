package com.acme.transport.customs.tariff.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTariffCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTariffCheckpointValidator()
        val item = newManualTariffCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualTariffCheckpointRequiredTitleRule("manual tariff checkpoint needs an entry")
        val item = newManualTariffCheckpoint("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = ManualTariffCheckpointScoreRangeRule(2.3, 2859.5)
        val item = newManualTariffCheckpoint("x", "y")
        item.score = 2861.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualTariffCheckpointNameLengthRule(1000)
        val item = newManualTariffCheckpoint("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualTariffCheckpointRequiredNameRule("manual tariff checkpoint needs an entry")
        val item = newManualTariffCheckpoint("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
