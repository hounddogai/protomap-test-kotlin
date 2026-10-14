package com.acme.transport.safety.cameraevent.defaulteventscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalCameraEventQuotaTest {
    @Test
    fun `starts in proposed`() {
        val item = newSeasonalCameraEventQuota("code-310", "name-721")
        assertEquals(SeasonalCameraEventQuotaStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newSeasonalCameraEventQuota("code-310", "name-721")
        try {
            item.moveTo(SeasonalCameraEventQuotaStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalCameraEventQuotaStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSeasonalCameraEventQuota("code-310", "name-721")
        item.status = SeasonalCameraEventQuotaStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SeasonalCameraEventQuotaStatus.PROPOSED) }
    }
}
