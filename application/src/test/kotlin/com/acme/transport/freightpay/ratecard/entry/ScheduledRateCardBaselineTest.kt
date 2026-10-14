package com.acme.transport.freightpay.ratecard.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRateCardBaselineTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledRateCardBaseline("code-213", "title-122")
        assertEquals(ScheduledRateCardBaselineStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newScheduledRateCardBaseline("code-213", "title-122")
        try {
            item.moveTo(ScheduledRateCardBaselineStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRateCardBaselineStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledRateCardBaseline("code-213", "title-122")
        item.status = ScheduledRateCardBaselineStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledRateCardBaselineStatus.CREATED) }
    }
}
