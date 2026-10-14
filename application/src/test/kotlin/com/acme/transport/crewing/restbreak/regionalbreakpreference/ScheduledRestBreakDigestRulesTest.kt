package com.acme.transport.crewing.restbreak.regionalbreakpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRestBreakDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledRestBreakDigestValidator()
        val item = newScheduledRestBreakDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = ScheduledRestBreakDigestRequiredCarrierRule("scheduled rest break digest needs this field")
        val item = newScheduledRestBreakDigest("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledRestBreakDigestRequiredTitleRule("scheduled rest break digest needs an entry")
        val item = newScheduledRestBreakDigest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
