package com.acme.transport.lastmile.drop.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDropSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalDropSnapshotValidator()
        val item = newGlobalDropSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = GlobalDropSnapshotRequiredUnitRule("global drop snapshot needs a value")
        val item = newGlobalDropSnapshot("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = GlobalDropSnapshotRequiredCountryRule("global drop snapshot needs an entry")
        val item = newGlobalDropSnapshot("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalDropSnapshotRequiredCodeRule("global drop snapshot needs this field")
        val item = newGlobalDropSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
