package com.acme.transport.freightpay.ratecard.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedRateCardScheduleTest {
    @Test
    fun `starts in created`() {
        val item = newCappedRateCardSchedule("code-675", "name-958")
        assertEquals(CappedRateCardScheduleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newCappedRateCardSchedule("code-675", "name-958")
        try {
            item.moveTo(CappedRateCardScheduleStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedRateCardScheduleStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedRateCardSchedule("code-675", "name-958")
        item.status = CappedRateCardScheduleStatus.RETIRED
        assertFails("move") { item.moveTo(CappedRateCardScheduleStatus.CREATED) }
    }
}
