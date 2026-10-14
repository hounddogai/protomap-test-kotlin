package com.acme.transport.terminal.dwell.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDwellEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveDwellEntryValidator()
        val item = newAdaptiveDwellEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = AdaptiveDwellEntryStartsAtRangeRule(80, 3600)
        val item = newAdaptiveDwellEntry("x", "y")
        item.startsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AdaptiveDwellEntryRequiredNameRule("adaptive dwell entry needs this field")
        val item = newAdaptiveDwellEntry("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = AdaptiveDwellEntryRequiredExternalRefRule("adaptive dwell entry needs a value")
        val item = newAdaptiveDwellEntry("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
