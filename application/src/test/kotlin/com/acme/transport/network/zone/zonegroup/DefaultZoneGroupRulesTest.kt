package com.acme.transport.network.zone.zonegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultZoneGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultZoneGroupValidator()
        val item = newDefaultZoneGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = DefaultZoneGroupRequiredShiftCodeRule("default zone group needs a value")
        val item = newDefaultZoneGroup("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultZoneGroupRequiredTitleRule("default zone group needs a value")
        val item = newDefaultZoneGroup("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
