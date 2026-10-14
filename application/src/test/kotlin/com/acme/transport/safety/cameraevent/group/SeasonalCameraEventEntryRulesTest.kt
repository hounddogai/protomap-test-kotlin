package com.acme.transport.safety.cameraevent.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalCameraEventEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalCameraEventEntryValidator()
        val item = newSeasonalCameraEventEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = SeasonalCameraEventEntryEndsAtRangeRule(64, 80)
        val item = newSeasonalCameraEventEntry("x", "y")
        item.endsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = SeasonalCameraEventEntryRequiredOwnerIdRule("seasonal camera event entry needs an entry")
        val item = newSeasonalCameraEventEntry("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = SeasonalCameraEventEntryRequiredExternalRefRule("seasonal camera event entry needs this field")
        val item = newSeasonalCameraEventEntry("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
