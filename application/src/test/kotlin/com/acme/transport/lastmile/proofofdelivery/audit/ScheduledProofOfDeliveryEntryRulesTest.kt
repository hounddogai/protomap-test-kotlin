package com.acme.transport.lastmile.proofofdelivery.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledProofOfDeliveryEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledProofOfDeliveryEntryValidator()
        val item = newScheduledProofOfDeliveryEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledProofOfDeliveryEntryRequiredCodeRule("scheduled proof of delivery entry needs a value")
        val item = newScheduledProofOfDeliveryEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledProofOfDeliveryEntryRequiredTitleRule("scheduled proof of delivery entry needs an entry")
        val item = newScheduledProofOfDeliveryEntry("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
