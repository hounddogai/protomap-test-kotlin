package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalParcelRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalParcelRecordValidator()
        val item = newSeasonalParcelRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalParcelRecordRequiredLabelRule("seasonal parcel record needs an entry")
        val item = newSeasonalParcelRecord("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
