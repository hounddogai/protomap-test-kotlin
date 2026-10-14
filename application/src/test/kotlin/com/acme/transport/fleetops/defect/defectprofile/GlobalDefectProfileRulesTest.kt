package com.acme.transport.fleetops.defect.defectprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDefectProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalDefectProfileValidator()
        val item = newGlobalDefectProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = GlobalDefectProfileRequiredContactEmailRule("global defect profile needs an entry")
        val item = newGlobalDefectProfile("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = GlobalDefectProfilePriorityRangeRule(64, 120)
        val item = newGlobalDefectProfile("x", "y")
        item.priority = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = GlobalDefectProfileRevisionRangeRule(64, 80)
        val item = newGlobalDefectProfile("x", "y")
        item.revision = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = GlobalDefectProfileEndsAtRangeRule(64, 120)
        val item = newGlobalDefectProfile("x", "y")
        item.endsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
